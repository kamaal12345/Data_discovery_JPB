import { PiiScanResult } from "./PiiScanResult";
export interface PiiScanRequest {
  requestId?: number;
  serverType: string;
  targetName: string;
  maxFileSize: number; 
  filePath: String;
  piiTypes: String;
  excludePatterns: String;
  stopScannAfter?: number;
  createdById: number;
  createdDate?: Date;
  updatedDate?: Date;
  progress?: number;
  piiScanResults?: PiiScanResult[];
}
