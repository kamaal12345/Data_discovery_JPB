export interface PiiScanResult {
  resultId?: number;
  piiType: string;
  filePath: string;
  matchedData: string;
  ip: string;
}
