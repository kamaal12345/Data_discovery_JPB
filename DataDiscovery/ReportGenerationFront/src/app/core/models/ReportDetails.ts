import { VulnerabilitiesSummary } from "./VulnerabilitiesSummary";

export interface ReportDetails {
    reportId: number;
    scope:string
    typeOfTesting: string;
    headerImage: string;
    headerText: string;
    footerText: string;
    applicationName: string;
    assessmentType: 0 | 1;
    companyName: string;
    companyLogo: string;
    createdDate: number;
    updatedDate: number;
    vulnerabilities: VulnerabilitiesSummary[];
    duplicateVulnerabilities?: VulnerabilitiesSummary[];
  }


  