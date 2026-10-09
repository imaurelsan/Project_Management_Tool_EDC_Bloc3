#requires -Version 5.1
<#[
.SYNOPSIS
    Crée et initialise la base MySQL du projet PMT avec Podman.
.DESCRIPTION
    Le script n'utilise pas Docker. Il démarre (ou réutilise) le conteneur
    pmt-mysql, attend la disponibilité de MySQL, applique le schéma complet,
    puis vérifie les tables et les colonnes attendues.
#>

[CmdletBinding()]
param(
    [int]$StartupTimeoutSeconds = 120
)

$ErrorActionPreference = 'Stop'

$ContainerName = 'pmt-mysql'
$DatabaseName = 'pmt_db'
$RootPassword = 'root'
$AppUser = 'pmt_user'
$AppPassword = 'root'
$Port = 3306

function Invoke-Podman {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    & podman @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "La commande Podman a échoué (code $LASTEXITCODE): podman $($Arguments -join ' ')"
    }
}

function Get-PodmanOutput {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    $output = & podman @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "La commande Podman a échoué (code $LASTEXITCODE): podman $($Arguments -join ' ')`n$($output -join "`n")"
    }
    return (($output -join "`n").Trim())
}

# Schéma complet. Le script est idempotent et peut être rejoué sans supprimer les données.
$sql = @'
CREATE DATABASE IF NOT EXISTS `pmt_db`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'pmt_user'@'%' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON `pmt_db`.* TO 'pmt_user'@'%';
FLUSH PRIVILEGES;

USE `pmt_db`;

CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(100) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `first_name` VARCHAR(100) NOT NULL,
    `last_name` VARCHAR(100) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_users_username` (`username`),
    UNIQUE KEY `uq_users_email` (`email`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `projects` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT NULL,
    `owner_id` BIGINT UNSIGNED NOT NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'active',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_projects_owner_id` (`owner_id`),
    CONSTRAINT `fk_projects_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `project_members` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT UNSIGNED NOT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `role` VARCHAR(50) NOT NULL,
    `joined_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_project_members_project_user` (`project_id`, `user_id`),
    KEY `idx_project_members_user_id` (`user_id`),
    CONSTRAINT `fk_project_members_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `fk_project_members_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `tasks` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT UNSIGNED NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT NULL,
    `assigned_to` BIGINT UNSIGNED NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'todo',
    `priority` VARCHAR(50) NOT NULL DEFAULT 'medium',
    `due_date` DATE NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_tasks_project_id` (`project_id`),
    KEY `idx_tasks_assigned_to` (`assigned_to`),
    CONSTRAINT `fk_tasks_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `fk_tasks_assigned_to` FOREIGN KEY (`assigned_to`) REFERENCES `users` (`id`)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `notifications` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `type` VARCHAR(100) NOT NULL,
    `message` TEXT NOT NULL,
    `read_status` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_notifications_user_id` (`user_id`),
    CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `history` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `entity_type` VARCHAR(100) NOT NULL,
    `entity_id` BIGINT UNSIGNED NOT NULL,
    `action` VARCHAR(100) NOT NULL,
    `changed_by` BIGINT UNSIGNED NOT NULL,
    `old_value` JSON NULL,
    `new_value` JSON NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_history_entity` (`entity_type`, `entity_id`),
    KEY `idx_history_changed_by` (`changed_by`),
    CONSTRAINT `fk_history_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;
'@

try {
    if (-not (Get-Command podman -ErrorAction SilentlyContinue)) {
        throw 'Podman est introuvable dans le PATH. Installez Podman puis relancez ce script.'
    }

    $existingId = (& podman ps -a --filter "name=^$ContainerName`$" --format '{{.ID}}' 2>$null | Select-Object -First 1)
    if ([string]::IsNullOrWhiteSpace($existingId)) {
        Write-Host "Démarrage du conteneur $ContainerName..."
        Invoke-Podman -Arguments @(
            'run', '-d', '--name', $ContainerName,
            '-e', "MYSQL_ROOT_PASSWORD=$RootPassword",
            '-e', "MYSQL_DATABASE=$DatabaseName",
            '-e', "MYSQL_USER=$AppUser",
            '-e', "MYSQL_PASSWORD=$AppPassword",
            '-p', "${Port}:3306",
            'mysql:8.0'
        ) | Out-Null
    }
    else {
        $runningId = (& podman ps --filter "name=^$ContainerName`$" --format '{{.ID}}' 2>$null | Select-Object -First 1)
        if ([string]::IsNullOrWhiteSpace($runningId)) {
            Write-Host "Le conteneur $ContainerName existe déjà et est arrêté; démarrage..."
            Invoke-Podman -Arguments @('start', $ContainerName) | Out-Null
        }
        else {
            Write-Host "Le conteneur $ContainerName est déjà en cours d'exécution."
        }
    }

    Write-Host 'Attente de la disponibilité de MySQL...'
    $deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
    $ready = $false
    while ((Get-Date) -lt $deadline) {
        & podman exec $ContainerName mysqladmin ping -uroot "-p$RootPassword" --silent 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            $ready = $true
            break
        }
        Start-Sleep -Seconds 2
    }
    if (-not $ready) {
        throw "MySQL n'est pas prêt après $StartupTimeoutSeconds secondes. Consultez: podman logs $ContainerName"
    }

    Write-Host 'Application du schéma MySQL...'
    $sql | & podman exec -i $ContainerName mysql -uroot "-p$RootPassword" --protocol=TCP
    if ($LASTEXITCODE -ne 0) {
        throw "L'application du script SQL a échoué (code $LASTEXITCODE)."
    }

    Write-Host 'Validation des tables et de la colonne notifications.created_at...'
    $tableOutput = Get-PodmanOutput -Arguments @(
        'exec', $ContainerName, 'mysql', '-uroot', "-p$RootPassword", '--batch', '--skip-column-names',
        '-e', "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DatabaseName' AND TABLE_NAME IN ('users','projects','project_members','tasks','notifications','history') ORDER BY TABLE_NAME;"
    )
    $expectedTables = @('history', 'notifications', 'project_members', 'projects', 'tasks', 'users')
    $actualTables = @($tableOutput -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Sort-Object)
    if (($actualTables -join '|') -ne ($expectedTables -join '|')) {
        throw "Validation échouée. Tables attendues: $($expectedTables -join ', '); trouvées: $($actualTables -join ', ')"
    }

    $createdAtCheck = Get-PodmanOutput -Arguments @(
        'exec', $ContainerName, 'mysql', '-uroot', "-p$RootPassword", '--batch', '--skip-column-names',
        '-e', "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='$DatabaseName' AND TABLE_NAME='notifications' AND COLUMN_NAME='created_at';"
    )
    if ($createdAtCheck.Trim() -ne '1') {
        throw 'Validation échouée: notifications.created_at est absente.'
    }

    Write-Host 'Terminé: les six tables PMT existent et notifications.created_at est présente.' -ForegroundColor Green
}
catch {
    Write-Error $_
    exit 1
}
