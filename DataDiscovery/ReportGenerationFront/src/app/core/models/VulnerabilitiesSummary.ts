export interface VulnerabilitiesSummary {
    id: number;
    vulId: string;
    severity: string;
    vulnerability: string;
    affectedScope: string;
    description: string;
    observation: string;
    testDetails: string;
    remediation: string;
    references: string;
    reportId: number;
    createdDate: number;
    updatedDate: number;
  }
  