import { PiiScanRequest } from "./PiiScanRequest";
import { SshConnection } from "./SshConnection";

export interface RemotePiiScanRequest extends PiiScanRequest {
  connection: SshConnection;
}
