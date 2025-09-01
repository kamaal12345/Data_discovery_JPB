export interface Host {
  id: number;
  name: string;
  ip: string;
  status: 0 | 1;
  category: number;
  description?: string;
  createdId: number;
  UpdatedId: number;
  createdDate?: number;
  updatedDate?: number;
}
