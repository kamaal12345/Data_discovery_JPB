export interface SshConnection {
  host: string[];
  port?: number;
  username: string;
  password: string;
  conPassword?: string;
}
