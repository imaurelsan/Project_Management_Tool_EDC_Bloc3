import { HttpErrorResponse } from '@angular/common/http';

export function apiErrorMessage(error: unknown): string {
  const status = error instanceof HttpErrorResponse ? error.status : 0;
  if (status === 400) return 'Les données saisies sont invalides.';
  if (status === 403) return 'Vous n’avez pas les droits pour cette action.';
  if (status === 404) return 'La ressource demandée est introuvable.';
  if (status === 409) return 'Cette donnée existe déjà.';
  return 'Une erreur est survenue. Réessayez plus tard.';
}
