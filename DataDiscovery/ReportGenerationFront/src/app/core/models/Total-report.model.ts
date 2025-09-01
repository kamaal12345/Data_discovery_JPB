import { ModificationHistory } from './ModificationHistory';
import { ReportDetails } from './ReportDetails';
import { VulnerabilitiesSummary } from './VulnerabilitiesSummary';

export interface TotalReport {
  reportDetails: ReportDetails;
  modificationHistory: ModificationHistory[];
  vulnerabilitiesSummaries: VulnerabilitiesSummary[];
}
