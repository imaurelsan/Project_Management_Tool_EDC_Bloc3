-- Migration de la table notification
-- Cible : pmt_db.notification
-- created_at correspond à la propriété JPA @Column(name = "created_at").

SET @db_name = DATABASE();

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE `notification` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',
        'SELECT 1'
    )
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'notification'
      AND COLUMN_NAME = 'created_at'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Validation post-migration
SHOW COLUMNS FROM `notification`;
