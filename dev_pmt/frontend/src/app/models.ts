export type ProjectRole = 'ADMINISTRATOR' | 'MEMBER' | 'VIEWER';
export type TaskStatus = 'A_FAIRE' | 'EN_COURS' | 'FAIT' | 'ANNULE';
export type TaskPriority = 'FAIBLE' | 'MOYEN' | 'HAUTE' | 'CRITIQUE';

export interface User {
  id: number;
  username: string;
  email: string;
}

export interface Project {
  id: number;
  name: string;
  description: string;
  startDate: string | null;
  creatorId: number;
  creatorUsername: string;
}

export interface Member {
  userId: number;
  username: string;
  email: string;
  role: ProjectRole;
}

export interface Task {
  id: number;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string | null;
  projectId: number;
  assigneeId: number | null;
  assigneeUsername: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface History {
  id: number;
  modifiedField: string;
  oldValue: string | null;
  newValue: string | null;
  modifiedDate: string;
  modifiedBy: string;
}

export interface Notification {
  id: number;
  message: string;
  read: boolean;
  createdAt: string;
  taskId: number;
  userId: number;
}

export interface ProjectRequest {
  name: string;
  description: string;
}

export interface TaskRequest {
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string | null;
  assigneeId: number | null;
}
