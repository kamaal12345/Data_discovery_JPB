import { Component, OnDestroy, OnInit } from '@angular/core';
import type {Content,TDocumentDefinitions,Alignment,} from 'pdfmake/interfaces';
import { PDFService } from '../../../core/services/pdf.service';
import { finalize, Subject, takeUntil } from 'rxjs';
import { GlobalService } from '../../../core/services/global-service.service';
import { ImageService } from '../../../core/services/image.service';
import { DocumentImage } from '../../../core/models/DocumentImage';
import { PdfTriggerService } from '../../../core/services/pdf-trigger.service';
import { ReportDetailsService } from '../../../core/services/report-details.service';
import { DomSanitizer } from '@angular/platform-browser';
import htmlToPdfmake from 'html-to-pdfmake';
import { SecurityContext } from '@angular/core';
import { TotalReport } from '../../../core/models/Total-report.model';
@Component({
  selector: 'app-pdf-generation',
  standalone: true,
  imports: [],
  templateUrl: './pdf-generation.component.html',
  styleUrl: './pdf-generation.component.css',
})
export class PdfGenerationComponent implements OnInit, OnDestroy {
  public destroy$: Subject<boolean>;
  isLoading: boolean;
  imagesList: DocumentImage[] = [];
  currentDate: string;
  toc: any[];
  reportId!: number;
  totalReportData: TotalReport;
  currentPdfAction: 'preview' | 'download' = 'preview';

  constructor(
    public pdfService: PDFService,
    public globalService: GlobalService,
    public imageService: ImageService,
    private pdfTriggerService: PdfTriggerService,
    public reportService: ReportDetailsService,
    private sanitizer: DomSanitizer
  ) {
    this.isLoading = false;
    this.destroy$ = new Subject<boolean>();
    this.currentDate = this.getShortFormattedDate();
  }

ngOnInit(): void {
  this.pdfTriggerService.triggerPdf$
    .pipe(takeUntil(this.destroy$))
    .subscribe(({ reportId, action }) => {
      this.reportId = reportId;
      this.currentPdfAction = action;
      this.getALLImagesList();
    });
}

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  getALLImagesList() {
    this.isLoading = true;
    this.imageService
      .imageList()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe({
        next: (data) => {
          console.log(data + 'from');
          this.imagesList = data;
          this.getTotalReport();
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete'),
      });
  }

  getTotalReport() {
    this.isLoading = true;
    this.reportService
      .getTotalReportData(this.reportId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe({
        next: (data) => {
          console.log(data + 'from');
          this.totalReportData = data;
          this.generatePdf();
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete'),
      });
  }

  generateCoverPage(): Content[] {
    return [
      {
        columns: [
          {
            text: 'CONFIDENTIAL',
            alignment: 'right',
            fontSize: 9,
            margin: [0, 10, 0, 5],
          },
        ],
        margin: [60, 0, 40, 72],
      },
      {
        image: this.totalReportData.reportDetails.companyLogo,
        width: 150,
        alignment: 'center',
        margin: [0, 20, 0, 80],
      },
      {
        columns: [
          {
            canvas: [
              {
                type: 'line',
                x1: 0,
                y1: 0,
                x2: 0,
                y2: 120,
                lineWidth: 2,
                lineColor: 'blue',
              },
            ],
            width: 10,
            margin: [0, 0, 10, 0],
          },
          {
            stack: [
              {
                text: this.totalReportData.reportDetails.applicationName,
                style: 'titleText',
              },
              this.emphasizeFirstLetters(
                // 'MOBILE    APPLICATION     SECURITY     TESTING     REPORT',
                this.totalReportData.reportDetails.typeOfTesting.toUpperCase().replace(/ /g, '    ') ,
                25,
                18,
                undefined,
                'left',
                [0, 0, 0, 10]
              ),
              { text: this.getLongFormattedDate(), style: 'dateText' },
            ],
            width: '*',
          },
        ],
        margin: [20, 0, 60, 60],
      },
      {
        table: {
          widths: ['auto'],
          body: [
            [
              {
                text: this.totalReportData.reportDetails.companyName,
                style: 'companyTitle',
              },
            ],
          ],
        },
        layout: {
          hLineWidth: () => 1,
          vLineWidth: () => 1,
          hLineColor: () => 'black',
          vLineColor: () => 'black',
          paddingLeft: () => 5,
          paddingRight: () => 5,
          paddingTop: () => 2,
          paddingBottom: () => 3,
        },
        alignment: 'center',
        margin: [150, 0, 40, 10],
      },
    ];
  }

  modificationHistorySection(): Content[] {
    return [
      {
        text: 'Modifications History',
        tocItem: true,
        id: 'modification-history',
        fontSize: 0,
        margin: [0, 0, 0, 0],
        color: 'white',
        bold: true,
      },
      this.emphasizeFirstLetters(
        'MODIFICATIONS HISTORY',
        16,
        12,
        undefined,
        'center'
      ),
      {
        table: {
          headerRows: 1,
          widths: ['auto', 'auto', '*', '*'],
          body: [
            [
              { text: 'Version', style: 'modhisTableHeader' },
              { text: 'Date', style: 'modhisTableHeader' },
              { text: 'Author', style: 'modhisTableHeader' },
              { text: 'Description', style: 'modhisTableHeader' },
            ],
            [
              { text: '1.0', style: 'modhisTableCell' },
              { text: this.currentDate, style: 'modhisTableCell' },
              { text: 'Puskar Sathe', style: 'modhisTableCell' },
              { text: 'Initial Version', style: 'modhisTableCell' },
            ],
            [{ text: '', style: 'modhisTableCell' }, '', '', ''],
            [{ text: '', style: 'modhisTableCell' }, '', '', ''],
            [{ text: '', style: 'modhisTableCell' }, '', '', ''],
            [{ text: '', style: 'modhisTableCell' }, '', '', ''],
          ],
        },
        layout: {
          fillColor: (rowIndex: number) => (rowIndex === 0 ? '#dbeafe' : null),
          hLineWidth: () => 0.5,
          vLineWidth: () => 0.5,
          hLineColor: () => 'black',
          vLineColor: () => 'black',
        },
      },
    ];
  }

  disclaimerSection(): Content[] {
    return [
      {
        text: 'Disclaimer',
        tocItem: false,
        id: 'disclaimer',
        fontSize: 0,
        margin: [0, 0, 0, 0],
        color: 'white',
        bold: true,
      },
      this.emphasizeFirstLetters(
        'DISCLAIMER',
        16,
        12,
        undefined,
        'center',
        undefined
      ),

      {
        text: `By accessing and using this report you agree to the following terms and conditions and all applicable laws, without limitation or qualification, unless otherwise stated, the contents of this document including, but not limited to, the text and images contained herein and their arrangement are the property of Infoprecept Consulting Pvt. Ltd. Nothing contained in this document shall be construed as conferring by implication, estoppels, or otherwise, any license or right to any copyright, patent, trademark or other proprietary interest of Infoprecept Consulting Pvt.Ltd. or any third party. This document and its contents including, but not limited to, graphic images and documentation may not be copied, reproduced, republished, uploaded, posted, transmitted, or distributed in any way, without the prior written consent of Infoprecept Consulting Pvt. Ltd. Any use you make of the information provided, is at your own risk and liability.
      Infoprecept Consulting Pvt. Ltd makes no representation about the suitability, reliability, availability, timeliness, and accuracy of the information, products, services, and related graphics contained in this document. All such information products, services, related graphics and other contents are provided 'as is' without warranty of any kind. The relationship between you and Infoprecept Consulting Pvt. Ltd shall be governed by the laws of the Republic of India without regard to its conflict of law provisions. You and Infoprecept Consulting Pvt. Ltd agree to submit to the personal and exclusive jurisdiction of the courts located at Ahmedabad. You are responsible for complying with the laws of the jurisdiction and agree that you will not access or use the information in this report, in violation of such laws. You represent that you have the lawful right to submit such information and agree that you will not submit any information unless you are legally entitled to do so.
      This report is being supplied by us on the basis that it is for your benefit and information only and that, save as may be required by law or by a competent regulatory authority (in which case you shall inform us in advance), it shall not be copied, referred to or disclosed, in whole or in part, without our prior written consent. The report is submitted on the basis that you shall not quote our name or reproduce our logo in any form or medium without prior written consent. You may disclose in whole this report to your legal and other professional advisers for the purpose of your seeking advice in relation to the report, provided that when doing so you inform them that Disclosure by them (save for their own internal purposes) is not permitted without our prior written consent, and to the fullest extent permitted by law we accept no responsibility or liability to them in connection with this report.
      Any advice, opinion, statement of expectation, forecast or recommendation supplied or expressed by us in this report is based on the information provided to us and we believe such advice, opinion, statement of expectation, forecast or recommendation to be sound. However, such advice, opinion, statement of expectation, forecast or recommendation shall not amount to any form of guarantee that we have determined or predicted future events or circumstances but shall ensure accuracy, competency, correctness or completeness of the report based on the information provided to us.`,
        style: 'disclaimerBody',
      },
    ];
  }

  confidentialitySection(): Content[] {
    return [
      {
        text: 'Confidentiality & Proprietary',
        tocItem: false,
        id: 'confidentialityProprietary',
        fontSize: 0,
        margin: [0, 0, 0, 0],
        color: 'white',
        bold: true,
      },
      this.emphasizeFirstLetters(
        'CONFIDENTIALITY & PROPRIETARY',
        16,
        12,
        undefined,
        'center',
        undefined
      ),
      {
        columns: [
          {
            image: this.getImageDataUrlByName('Documentwithlock'),
            width: 30,
            height: 30,
          },
          {
            text: 'This document consists of confidential information which should not be disclosed, transmitted,',
            style: 'confidentialBody',
          },
        ],
      },
      {
        text: 'duplicated or used in whole or part for any purpose other than the intended one outside Jio Payments Bank LTD.Any use of this document without explicit permission of Jio Payments Bank LTD is prohibited.',
        style: 'secondLine',
      },

      { text: 'Report Analysis', style: 'confidentialSectionTitle' },

      {
        columns: [
          {
            image: this.getImageDataUrlByName('Notepad'),
            width: 30,
            height: 30,
          },
          {
            text: 'The issues identified in this document are based upon our assessment testing effort. We made',
            style: 'confidentialBody',
          },
        ],
      },
      {
        text: `specific efforts to verify the accuracy and authenticity of the issues we have highlighted. The audit was held between ${this.currentDate} and ${this.currentDate}.`,
        style: 'secondLine',
      },
      {
        text: `The issues reported in this report are valid till ${this.currentDate}. Any vulnerability or exploit discovered after ${this.currentDate} will not be covered by this report. Any changes made to the hosts' software/hardware or configuration will affect the security architecture of the client's environment. Whenever such updates occur, we recommend conducting a penetration test to ensure the security posture is not compromised by these changes.`,
        style: 'secondLine',
      },
    ];
  }

  executiveSummary(): Content[] {
    return [
      {
        text: 'Executive Summary',
        tocItem: true,
        id: 'executiveSummary',
        fontSize: 0,
        margin: [0, 0, 0, 0],
        color: 'white',
        bold: true,
      },
      this.emphasizeFirstLetters(
        'EXECUTIVE SUMMARY',
        16,
        12,
        undefined,
        'center',
        undefined
      ),
      {
        columns: [
          {
            image: this.getImageDataUrlByName('Documentwithuser'),
            width: 30,
            height: 30,
          },
          {
            text: 'Jio Payments Bank LTD engaged with Infopercept Consulting Pvt. Ltd to conduct a security assessment',
            style: 'confidentialBody',
          },
        ],
      },
      {
        text: 'of its Mobile Application. The purpose of the engagement is to utilize active vulnerability assessment techniques to evaluate the security of the Mobile Application against best practice criteria and to validate its security mechanisms and identify vulnerabilities.',
        style: 'secondLine',
      },
      {
        text: 'This security assessment will provide Jio Payments Bank LTD with insight into the resilience of its Mobile Application to withstand attack from unauthorized users to abuse privileges and access of legitimate users.',
        style: 'secondLine',
      },
      {
        canvas: [
          {
            type: 'line',
            x1: 0,
            y1: 0,
            x2: 500,
            y2: 0,
            lineWidth: 1,
          },
        ],
        margin: [0, 10, 0, 0],
      },
      {
        text: 'Audit Scope',
        tocItem: true,
        id: 'audit-scope',
        tocMargin: [10, 0, 0, 0],
        fontSize: 0,
        color: 'white',
        bold: true,
      },

      {
        text: 'Audit Scope',
        style: 'centeredTitle',
      },
      {
        columns: [
          {
            image: this.getImageDataUrlByName('Documentwithpercentage'),
            width: 30,
            height: 30,
          },
          {
            text: 'Following application is included in the scope:',
            style: 'reportParagraph',
          },
        ],
      },
      {
        table: {
          widths: ['*'],
          body: [
            [
              {
                ul: [
                  {
                    text: this.totalReportData.reportDetails.scope,
                    style: 'reportListItem',
                  },
                ],
              },
            ],
          ],
        },
        layout: {
          hLineWidth: () => 0.5,
          vLineWidth: () => 0.5,
          hLineColor: () => 'black',
          vLineColor: () => 'black',
          paddingLeft: () => 5,
          paddingRight: () => 5,
          paddingTop: () => 2,
          paddingBottom: () => 2,
        },
        margin: [0, 5, 25, 10],
      },
      { text: 'Type of Assessment', style: 'reportSubheader' },
      {
        table: {
          headerRows: 1,
          widths: ['*', '*'],
          body: [
            [
              { text: 'Application Name', style: 'reportTableHeader' },
              {
                text: 'Assessment Type (Authenticated/Unauthenticated)',
                style: 'reportTableHeader',
              },
            ],
            [
              {
                text: this.totalReportData.reportDetails.applicationName,
                style: 'reportTableCell',
              },
              {
                text: this.totalReportData.reportDetails.assessmentType
                  ? 'Authenticated'
                  : 'Unauthenticated',
                style: 'reportTableCell',
              },
            ],
          ],
        },
        layout: {
          hLineWidth: () => 0.5,
          vLineWidth: () => 0.5,
          hLineColor: () => 'black',
          vLineColor: () => 'black',
          paddingLeft: () => 2,
          paddingRight: () => 2,
          paddingTop: () => 1.5,
          paddingBottom: () => 1.5,
        },
        margin: [0, 5, 25, 20],
      },
      {
        canvas: [
          {
            type: 'line',
            x1: 0,
            y1: 0,
            x2: 500,
            y2: 0,
            lineWidth: 1,
          },
        ],
        margin: [0, 10, 0, 0],
      },
      {
        text: 'Progress Report',
        tocItem: true,
        id: 'progress-report',
        tocMargin: [10, 0, 0, 0],
        fontSize: 0,
        color: 'white',
        bold: true,
      },

      {
        text: 'Progress Report',
        style: 'centeredTitle',
      },
      {
        columns: [
          {
            image: this.getImageDataUrlByName('Linebarchart'),
            width: 30,
            height: 30,
          },
          {
            text: 'This section of the report includes a complete project execution task and its status',
            style: 'reportParagraph',
          },
        ],
      },
      {
        table: {
          headerRows: 1,
          widths: ['45%', '55%'],
          body: [
            [
              {
                text: 'Tasks',
                colSpan: 2,
                style: 'reportTableHeader',
                alignment: 'center',
              },
              {},
            ],
            [
              {
                text: 'Project Planning and Kick-Off Meeting',
                style: 'reportTaskTableCell',
              },
              {
                text: 'Project initiation, Pre-engagement meetings, Defining Application VAPT scope and type of technologies in front end and backend that is used',
                style: 'reportTaskTableCell',
              },
            ],
            [
              {
                text: 'Vulnerability Identification',
                style: 'reportTaskTableCell',
              },
              {
                text: 'Finding application entry points and mapping business logical issues as per OWASP top 10 Methodology',
                style: 'reportTaskTableCell',
              },
            ],
            [
              {
                text: 'Vulnerability Verification',
                style: 'reportTaskTableCell',
              },
              {
                text: 'Finding and verifying vulnerabilities and eliminating false positive',
                style: 'reportTaskTableCell',
              },
            ],
            [
              {
                text: 'Penetration Testing',
                style: 'reportTaskTableCell',
              },
              {
                text: 'Exploitation and post exploitation to run remote code execution',
                style: 'reportTaskTableCell',
              },
            ],
            [
              {
                text: 'Reporting',
                style: 'reportTaskTableCell',
              },
              {
                text: 'Generating detailed technical report with findings, POCs, steps to replicate, specific recommendation',
                style: 'reportTaskTableCell',
              },
            ],
          ],
        },
        layout: {
          hLineWidth: () => 0.5,
          vLineWidth: () => 0.5,
          hLineColor: () => 'black',
          vLineColor: () => 'black',
          paddingLeft: () => 2,
          paddingRight: () => 2,
          paddingTop: () => 1.5,
          paddingBottom: () => 2,
        },
        margin: [0, 5, 25, 20],
      },
    ];
  }

  MethodologySection(): Content[] {
    return [
      {
        text: 'Infopercept Consulting PVT. LTD Pentest Methodology',
        tocItem: true,
        id: 'methodology',
        fontSize: 0,
        margin: [0, 0, 0, 0],
        color: 'white',
        bold: true,
      },
      this.emphasizeFirstLetters(
        'INFOPERCEPT CONSULTING PVT. LTD PENTEST METHODOLOGY',
        16,
        12,
        undefined,
        'center',
        undefined
      ),
      {
        columns: [
          {
            image: this.getImageDataUrlByName('Settinggears'),
            width: 20,
            height: 20,
          },
          {
            text: 'Our methodology is to provide Jio Payments bank LTD an in-depth overall security posture of their in-scope',
            style: 'confidentialBody',
          },
        ],
      },
      {
        text: 'targets.To achieve this task,we have used below pentest methodology along with OWASP Top 10 Mobile and SANS25',
        style: 'secondLine',
      },
      {
        table: {
          widths: ['*'],
          body: [
            [
              {
                image: this.getImageDataUrlByName('Methodology'),
                width: 500,
                height: 250,
              },
            ],
          ],
        },
        layout: {
          hLineWidth: () => 1,
          vLineWidth: () => 1,
          hLineColor: () => 'black',
          vLineColor: () => 'black',
          paddingLeft: () => 2,
          paddingRight: () => 2,
          paddingTop: () => 2,
          paddingBottom: () => 2,
        },
      },
    ];
  }

  vulnerabilitiesSummaries(): Content[] {
    return [
      {
        text: 'vulnerabilities Summary',
        tocItem: true,
        id: 'vulnerabilitiesSummary',
        fontSize: 0,
        margin: [0, 0, 0, 0],
        color: 'white',
        bold: true,
      },
      this.emphasizeFirstLetters(
        'VULNERABILITIES SUMMARY',
        18,
        12,
        undefined,
        'center',
        undefined
      ),
      {
        text: 'Following vulnerabilities have been discovered:',
        margin: [0, 0, 0, 5],
        fontSize: 10,
      },
      {
        table: {
          headerRows: 1,
          widths: ['15%', '10%', '*', '*'],
          body: [
            [
              { text: 'Risk', style: 'summaryTableHeader' },
              { text: 'ID', style: 'summaryTableHeader' },
              { text: 'Vulnerability', style: 'summaryTableHeader' },
              { text: 'Affected Scope', style: 'summaryTableHeader' },
            ],
            ...this.totalReportData.vulnerabilitiesSummaries.map((vul) => [
              {
                text: vul.severity,
                fillColor: this.getSeverityColor(vul.severity),
                color: 'white',
                style: 'summaryTableCell',
              },
              {
                stack: this.renderHtml(vul.vulId),
                style: 'summaryTableCell',
              },
              {
                stack: this.renderHtml(vul.vulnerability),
                style: 'summaryTableCell',
              },
              {
                stack: this.renderHtml(vul.affectedScope),
                style: 'summaryTableCell',
                alignment: 'left',
              },
            ]),
          ],
        },
        layout: {
          fillColor: (rowIndex: number) => (rowIndex === 0 ? '#D3D3D3' : null),
          hLineWidth: () => 0.5,
          vLineWidth: () => 0.5,
          hLineColor: () => 'black',
          vLineColor: () => 'black',
          paddingLeft: () => 2,
          paddingRight: () => 2,
          paddingTop: () => 1,
          paddingBottom: () => 1,
        },
        margin: [0, 5, 0, 10],
      },
    ];
  }

  technicalDetails(): Content[] {
    return [
      {
        text: 'Technical Details',
        tocItem: true,
        id: 'technicalDetails',
        fontSize: 0,
        margin: [0, 0, 0, 0],
        color: 'white',
        bold: true,
      },
      this.emphasizeFirstLetters(
        'TECHNICAL DETAILS',
        18,
        12,
        undefined,
        'center',
        undefined
      ),
      {
        canvas: [
          {
            type: 'line',
            x1: 0,
            y1: 0,
            x2: 500,
            y2: 0,
            lineWidth: 1,
          },
        ],
        margin: [0, 0, 0, 5],
      },
      ...this.totalReportData.vulnerabilitiesSummaries.map((vul, index) => {
        const startOnNewPage = index !== 0;

        const subtitle: Content = {
          stack: [
            {
              text: this.plainTextFromHtml(vul.vulnerability),
              tocItem: true,
              id: `vul-${index}`,
              tocMargin: [10, 0, 0, 0],
              fontSize: 0,
              color: 'white',
              bold: true,
              ...(startOnNewPage ? { pageBreak: 'before' } : {}),
            },
            {
              stack: this.renderHtml(vul.vulnerability),
              style: 'vulSubTitle',
              margin: [0, 0, 0, 10],
              tocItem: 'technicalDetails',
            },
          ],
        };

        const tableContent: Content = {
          table: {
            widths: ['20%', '80%'],
            body: [
              [
                { text: 'SEVERITY', style: 'detailsTableHeader' },
                {
                  text: vul.severity,
                  style: 'severityMedium',
                  fillColor: this.getSeverityColor(vul.severity),
                },
              ],
              [
                { text: 'AFFECTED SCOPE', style: 'detailsTableHeader' },
                {
                  stack: this.renderHtml(vul.affectedScope),
                  style: 'detailsTableCell',
                },
              ],
              [
                { text: 'DESCRIPTION', style: 'detailsTableHeader' },
                {
                  stack: this.renderHtml(vul.description),
                  style: 'detailsTableCell',
                },
              ],
              [
                { text: 'OBSERVATION', style: 'detailsTableHeader' },
                {
                  stack: this.renderHtml(vul.observation),
                  style: 'detailsTableCell',
                },
              ],
              [
                {
                  text: 'TEST DETAILS',
                  style: 'detailsTableHeader',
                  colSpan: 2,
                  alignment: 'left',
                  margin: [5, 5, 5, 5],
                },
                {},
              ],
              [
                {
                  colSpan: 2,
                  stack: this.renderHtml(vul.testDetails, true, index + 1),
                  style: 'detailsTableCell',
                },
                {},
              ],
              [
                { text: 'REMEDIATION', style: 'detailsTableHeader' },
                {
                  stack: this.renderHtml(vul.remediation),
                  style: 'detailsTableCell',
                },
              ],
              [
                { text: 'REFERENCES', style: 'detailsTableHeader' },
                {
                  stack: this.renderHtml(vul.references),
                  style: 'detailsTableCell',
                },
              ],
            ],
          },
          layout: {
            hLineWidth: () => 0.5,
            vLineWidth: () => 0.5,
            hLineColor: () => 'black',
            vLineColor: () => 'black',
            paddingLeft: () => 2,
            paddingRight: () => 2,
            paddingTop: () => 1,
            paddingBottom: () => 1,
          },
          margin: [0, 0, 0, 20],
        };

        return {
          stack: [subtitle, tableContent],
        };
      }),
    ];
  }

  AuditorsEndNotesSection(): Content[] {
    return [
      {
        text: "Auditor's End Notes",
        tocItem: true,
        id: 'auditorsEndNotes',
        fontSize: 0,
        margin: [0, 0, 0, 0],
        color: 'white',
        bold: true,
      },
      this.emphasizeFirstLetters(
        "AUDITOR'S END NOTES",
        16,
        12,
        undefined,
        'center',
        undefined
      ),
      {
        text:
          'During course of the audit, the auditors identified certain points that could be potential security concerns. ' +
          'This activity is done considering the scope, boundary and defined timeline, as this is a business-critical ' +
          'application it is highly recommended to conduct the assessment at regular interval.',
        style: 'executivParagraph',
      },
      {
        text:
          'Note: There may be other parameters in the application which are vulnerable other than the ones mentioned in the ' +
          "report; hence, it's always recommended that the same remediation should be applied in all the parameters over the " +
          'entire application and not only on the URLs/Parameters which are mentioned in the report.',
        style: 'executivParagraph',
      },
      {
        text:
          'While no system or application can be guaranteed to be 100% secure, and despite the thoroughness of the ' +
          'penetration testing process, there may still exist vulnerabilities that were not identified or are outside the ' +
          'scope of the engagement. However, due to the evolving nature of security threats and the limitations inherent in ' +
          'penetration testing, we do not make any guarantees regarding the future security of the application. The Client is ' +
          'advised to continue regular security assessments and implement ongoing security best practices to mitigate any ' +
          'potential emerging threats.',
        style: 'executivParagraph',
      },
      {
        text:
          'This section gives a brief description of each of the vulnerability reported and column to submit the retest ' +
          'report once the patching is done by the development team:',
        style: 'executivParagraph',
      },
      {
        text: 'ACTION ITEMS AND RETEST COMPLIANCE CHECK',
        style: 'reportParagraph',
        margin: [0, 10, 0, 20],
      },
      {
        table: {
          headerRows: 1,
          widths: [17, '*', '20%', '15%', '25%'],
          body: [
            [
              { text: 'No.', style: 'AuditorTableHeader' },
              { text: 'Vulnerability Details', style: 'AuditorTableHeader' },
              { text: 'Responsibility to close', style: 'AuditorTableHeader' },
              { text: 'Timeline', style: 'AuditorTableHeader' },
              { text: 'Retest Comply (Y/N)', style: 'AuditorTableHeader' },
            ],
            ...Array.from({ length: 7 }).map((_, i) => [
              `${i + 1}.`,
              '',
              '',
              '',
              '',
            ]),
          ],
        },
        layout: {
          hLineWidth: () => 0.5,
          vLineWidth: () => 0.5,
          hLineColor: () => 'black',
          vLineColor: () => 'black',
          paddingLeft: () => 4,
          paddingRight: () => 4,
          paddingTop: () => 8,
          paddingBottom: () => 8,
        },
      },
    ];
  }

  async generatePdf() {
    const docDefinition: TDocumentDefinitions = {
      pageSize: 'A4',
      pageMargins: [60, 72, 40, 72],
      header: this.headerFn,
      footer: this.footerFn,
      content: [
        ...this.generateCoverPage(),
        { text: '', pageBreak: 'after' },
        ...this.modificationHistorySection(),
        { text: '', pageBreak: 'after' },
        // ...this.disclaimerSection(),
        // { text: '', pageBreak: 'after' },
        // ...this.confidentialitySection(),
        // { text: '', pageBreak: 'after' },
        this.generateTocContent(),
        { text: '', pageBreak: 'after' },
        ...this.executiveSummary(),
        { text: '', pageBreak: 'after' },
        ...this.MethodologySection(),
        { text: '', pageBreak: 'after' },
        ...this.vulnerabilitiesSummaries(),
        { text: '', pageBreak: 'after' },
        ...this.technicalDetails(),
        { text: '', pageBreak: 'after' },
        ...this.AuditorsEndNotesSection(),
      ],

      styles: {
        hiddenTocEntry: {
          fontSize: 0,
          color: 'white',
          margin: [0, 0, 0, 0],
        },
        // title
        titleText: {
          fontSize: 12,
          bold: true,
          margin: [0, 10, 0, 10],
        },
        dateText: {
          fontSize: 10,
          color: 'black',
        },
        companyTitle: {
          color: '#FEC12C',
          fontSize: 20,
          bold: true,
          alignment: 'center',
        },

        //disclaimer
        disclaimerBody: {
          fontSize: 10,
          lineHeight: 1.5,
          alignment: 'justify',
          margin: [0, 0, 0, 10],
        },

        // Modification history table
        modhisTableHeader: {
          fontSize: 10,
          bold: true,
          alignment: 'center',
          margin: [1, 1, 1, 1],
        },

        modhisTableCell: {
          fontSize: 9.7,
          alignment: 'center',
          margin: [4, 4, 4, 4],
        },

        // confidential
        confidentialBody: {
          fontSize: 10,
          lineHeight: 1.3,
          alignment: 'justify',
          margin: [2, 20, 0, 0],
        },
        secondLine: {
          alignment: 'justify',
          lineHeight: 1.5,
          fontSize: 9.7,
          margin: [2, 0, 0, 0],
        },
        confidentialSectionTitle: {
          fontSize: 12,
          bold: true,
          margin: [0, 10, 0, 2],
        },

        //executive
        executivParagraph: {
          fontSize: 9.7,
          alignment: 'justify',
          lineHeight: 1.5,
          margin: [0, 2, 0, 6],
          color: '#333333',
        },

        // Titles
        summaryTitle: {
          fontSize: 16,
          bold: true,
          alignment: 'center',
          margin: [0, 0, 0, 10],
          color: '#1e3a8a',
        },
        detailsTitle: {
          fontSize: 16,
          alignment: 'center',
          margin: [0, 0, 0, 10],
          color: '#065f46',
        },
        vulSubTitle: {
          fontSize: 10,
          bold: true,
          margin: [0, 10, 0, 6],
          color: '#334155',
        },

        //
        centeredTitle: {
          alignment: 'center',
          bold: true,
          fontSize: 14,
          margin: [0, 2, 0, 20],
        },
        reportSubheader: {
          fontSize: 10,
          bold: true,
          margin: [0, 5, 0, 0],
          color: '#2c3e50',
          noWrap: true,
        },
        reportParagraph: {
          fontSize: 9,
          margin: [2, 20, 0, 0],
          color: '#333333',
        },
        reportTableHeader: {
          bold: true,
          fontSize: 10,
          fillColor: '#eeeeee',
          margin: [0, 1, 0, 1],
          noWrap: true,
          alignment: 'center',
        },

        AuditorTableHeader: {
          fontSize: 10,
          bold: false,
          alignment: 'center',
          margin: [0, 0, 0, 20],
        },

        reportTableCell: {
          fontSize: 9,
          alignment: 'center',
          margin: [0, 5, 0, 5],
        },
        reportTaskTableCell: {
          fontSize: 9,
          alignment: 'left',
          margin: [0, 0, 0, 4],
        },
        reportListItem: {
          fontSize: 9,
          margin: [15, 2, 0, 2],
        },

        // Summary table
        summaryTableHeader: {
          fontSize: 11,
          bold: true,
          color: 'black',
          alignment: 'center',
          margin: [0, 5],
        },
        summaryTableCell: {
          fontSize: 9,
          alignment: 'center',
          margin: [0, 3],
        },
        summaryTableStyle: {
          margin: [0, 5, 0, 20],
        },

        // Technical details table
        detailsTableHeader: {
          fontSize: 10,
          bold: true,
          margin: [2, 2, 2, 2],
        },
        detailsTableCell: {
          fontSize: 9,
          margin: [2, 0, 2, 2],
        },
        severityMedium: {
          color: 'white',
          bold: true,
          fillColor: '#facc15',
          alignment: 'center',
          fontSize: 9,
          margin: [4, 4, 4, 4],
        },
        tocText: {
          fontSize: 10,
          bold: true,
        },
        tocDots: {
          fontSize: 12,
          color: '#888',
          characterSpacing: 1,
        },
        tocPage: {
          fontSize: 10,
          alignment: 'right',
          bold: true,
        },
      },
    };

     if (this.currentPdfAction === 'download') {
    const fileName = `Report_${this.reportId}.pdf`;
    await this.pdfService.savePdf(docDefinition, fileName);
  } else {
    await this.pdfService.open(docDefinition);
  }
  }

  // renderHtml(html: string, isTestDetails: boolean = false): any[] {
  //   const sanitized = this.sanitizer.sanitize(SecurityContext.HTML, html) || '';
  //   const converted = htmlToPdfmake(sanitized);

  //   if (isTestDetails) {
  //     const array = Array.isArray(converted) ? converted : [converted];
  //     return this.wrapImagesInOrder(array, 400);
  //   }
  //   return [converted];
  // }

  renderHtml(
    html: string,
    isTestDetails: boolean = false,
    pocBaseIndex: number = 1
  ): any[] {
    const sanitized = this.sanitizer.sanitize(SecurityContext.HTML, html) || '';
    const converted = htmlToPdfmake(sanitized);
    const array = Array.isArray(converted) ? converted : [converted];

    if (isTestDetails) {
      return this.wrapImagesInOrder(array, 400, pocBaseIndex);
    }

    return [converted];
  }

  plainTextFromHtml(html: string): string {
    const div = document.createElement('div');
    div.innerHTML = html;
    return div.textContent || div.innerText || '';
  }

  wrapImagesInOrder(
    content: any[],
    width: number,
    pocBaseIndex: number
  ): any[] {
    let imageCounter = 0;

    return content.flatMap((item) => {
      if (item.image) {
        const pocLabel =
          imageCounter === 0
            ? `POC-${pocBaseIndex}`
            : `POC-${pocBaseIndex}.${imageCounter}`;
        imageCounter++;

        return [
          {
            table: {
              widths: ['*'],
              body: [
                [
                  {
                    image: item.image,
                    width: width,
                    alignment: 'center',
                    margin: [5, 5, 5, 5],
                  },
                ],
              ],
            },
            layout: {
              hLineColor: () => 'black',
              vLineColor: () => 'black',
              hLineWidth: () => 0.5,
              vLineWidth: () => 0.5,
            },
            margin: [5, 5, 5, 0],
          },
          {
            text: pocLabel,
            alignment: 'center',
            margin: [0, 2, 0, 10],
            fontSize: 10,
            bold: true,
          },
        ];
      } else if (item.stack) {
        return [
          {
            ...item,
            stack: this.wrapImagesInOrder(item.stack, width, pocBaseIndex),
          },
        ];
      } else if (Array.isArray(item.columns)) {
        return [
          {
            ...item,
            columns: this.wrapImagesInOrder(item.columns, width, pocBaseIndex),
          },
        ];
      }

      if (item.text) {
        return [
          {
            ...item,
            alignment: 'center',
            margin: [0, 2, 0, 2],
          },
        ];
      }

      return [item];
    });
  }

  getSeverityColor(severity: string): string {
    if (!severity) return 'green';
    const s = severity.toLowerCase();
    return s === 'high' ? '#dc2626' : s === 'medium' ? '#facc15' : '#16a34a';
  }

  getShortFormattedDate(): string {
    const date = new Date();
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
  }

  getLongFormattedDate(): string {
    const date = new Date();
    const weekday = date
      .toLocaleDateString('en-US', { weekday: 'long' })
      .toUpperCase();
    const month = date
      .toLocaleDateString('en-US', { month: 'long' })
      .toUpperCase();
    const day = date.getDate();
    const year = date.getFullYear();

    return `${weekday}, ${month} ${day}, ${year}`;
  }

  getImageDataUrlByName(iconName: string): string | null {
    const image = this.imagesList.find((img) => img.name === iconName);
    if (image) {
      return image.dataUrl;
    }
    console.error(`Icon with name ${iconName} not found`);
    return null;
  }

  headerFn = (currentPage: number): Content | null => {
    if (currentPage === 1) return null;
    return {
      stack: [
        {
          columns: [
            {
              image: this.totalReportData.reportDetails.headerImage,
              width: 600,
              alignment: 'center',
              margin: [0, 7, 7, 0],
            },
            // {
            //   text: 'CONFIDENTIAL',
            //   alignment: 'right',
            //   fontSize: 9,
            //   margin: [0, 34.5, 0, 5],
            //   height: 40,
            // },
          ],
          // margin: [60, 0, 40, 0],
        },
        // {
        //   canvas: [
        //     {
        //       type: 'line',
        //       x1: 0,
        //       y1: 0,
        //       x2: 500,
        //       y2: 0,
        //       lineWidth: 2,
        //     },
        //   ],
        //   margin: [60, 19.5, 40, 14],
        // },
      ],
    };
  };

  footerFn = (currentPage: number, pageCount: number): Content | null => {
    const startPageForNumbering = 2;
    if (currentPage < startPageForNumbering) return null;

    return {
      columns: [
        {
          text: `${this.totalReportData.reportDetails.typeOfTesting + " " + this.totalReportData.reportDetails.footerText} - ${this.currentDate}`,
          alignment: 'left',
          fontSize: 8,
          margin: [60, 30, 0, 0],
          noWrap: true,
        },
        {
          text: `${currentPage} / ${pageCount}`,
          alignment: 'right',
          fontSize: 8,
          margin: [0, 30, 40, 0],
          width: 'auto',
        },
      ],
    };
  };

  emphasizeFirstLetters(
    text: string,
    largeFontSize: number,
    smallFontSize: number,
    pageBreak?: 'before' | 'after',
    alignment: Alignment = 'center',
    margin: [number, number, number, number] = [0, 0, 0, 20],
    id?: string
  ): Content {
    const splitText = text.split(' ');

    const formattedText = splitText.flatMap((word) => {
      const firstLetter = word.charAt(0);
      const remainingLetters = word.slice(1);

      return [
        { text: firstLetter, fontSize: largeFontSize, bold: true },
        { text: remainingLetters, fontSize: smallFontSize, bold: true },
        { text: ' ' },
      ];
    });

    const content: Content = {
      text: formattedText,
      alignment,
      margin,
      ...(pageBreak ? { pageBreak } : {}),
      ...(id ? { id } : {}),
    };

    return content;
  }

  // generateTocContent(): Content[] {
  //   const tocItems = [
  //     { title: 'Modifications History', page: 1 },
  //     { title: 'Disclaimer', page: 2 },
  //     { title: 'Confidentiality & Proprietary', page: 3 },
  //     { title: 'Executive Summary', page: 6 },
  //     { title: 'Audit Scope', page: 6 },
  //     { title: 'Progress Report', page: 6 },
  //     { title: 'Infopercept Consulting Pvt. Ltd Pentest Methodology', page: 7 },
  //     { title: 'Vulnerabilities Summary', page: 8 },
  //     { title: 'Technical Details', page: 9 },
  //     ...(this.totalReportData?.vulnerabilitiesSummaries || []).map(
  //       (v: any, i: number) => ({
  //         title: this.removeHtmlTags(v.vulnerability),
  //         page: i + 12,
  //       })
  //     ),
  //   ];

  //   return [
  //     this.emphasizeFirstLetters('TABLE OF CONTENTS', 18, 12),
  //     ...tocItems.flatMap((item) =>
  //       this.addTocItemWithColumns(item.title, item.page)
  //     ),
  //   ];
  // }

  // generateTocContent(): Content[] {
  //   const tocItems = [
  //     { title: 'Modifications History' },
  //     { title: 'Disclaimer' },
  //     { title: 'Confidentiality & Proprietary' },
  //     { title: 'Executive Summary' },
  //     { title: 'Audit Scope' },
  //     { title: 'Progress Report' },
  //     { title: 'Infopercept Consulting Pvt. Ltd Pentest Methodology' },
  //     { title: 'Vulnerabilities Summary' },
  //     { title: 'Technical Details' },
  //     ...(this.totalReportData?.vulnerabilitiesSummaries || []).map(
  //       (v: any, i: number) => ({
  //         title: this.removeHtmlTags(v.vulnerability),
  //         tocItem: `vul-${i}`,
  //       })
  //     ),
  //     { title: "Auditor's End Notes", id: 'auditorsEndNotes' },
  //   ];
  //   return [
  //     this.emphasizeFirstLetters(
  //       'TABLE OF CONTENTS',
  //       18,
  //       12,
  //       undefined,
  //       'center',
  //       undefined,
  //       // true,
  //       // 'TableOfContent'
  //     ),
  //     ...tocItems.map((item, index) =>
  //       this.addTocItemWithColumns(item.title, index + 1)
  //     ),
  //   ];
  // }

  generateTocContent(): Content {
    return {
      toc: {
        title: {
          text: this.emphasizeFirstLetters(
            'TABLE OF CONTENTS',
            16,
            12,
            undefined,
            'center',
            undefined
          ),
          style: 'centeredTitle',
        },
        numberStyle: 'tocPage',
        textStyle: 'tocText',
      },
    };
  }

  removeHtmlTags(html: string): string {
    const doc = new DOMParser().parseFromString(html, 'text/html');
    return doc.body.textContent || '';
  }

  addTocItemWithColumns(title: string, page: number): any {
    const maxLineLength = 112;
    const pageStr = page.toString();

    if (title.length <= maxLineLength) {
      return this.buildTocLine(title, pageStr, true);
    }

    const breakIndex = title.lastIndexOf(' ', maxLineLength);
    const splitPoint = breakIndex > 0 ? breakIndex : maxLineLength;

    const firstLine = title.slice(0, splitPoint).trim();
    const remaining = title.slice(splitPoint).trim();

    return [
      this.buildTocLine(firstLine, '', false), // No page, no dots
      this.buildTocLine(remaining, pageStr, true), // With page and dots (all on one line)
    ];
  }

  buildTocLine(
    title: string,
    pageStr: string = '',
    showDots: boolean = true
  ): any {
    const totalWidth = 112;
    const titleLength = title.length;
    const pageLength = pageStr.length;

    const dotsLength =
      pageStr && showDots
        ? Math.max(0, totalWidth - titleLength - pageLength - 2)
        : 0;

    const dots = '.'.repeat(dotsLength);

    return {
      columns: [
        { text: title, style: 'tocText', width: 'auto', noWrap: true },
        {
          text: showDots ? dots : '',
          style: 'tocDots',
          width: '*',
          alignment: 'center',
        },
        { text: pageStr, style: 'tocPage', width: 20, alignment: 'right' },
      ],
      columnGap: 2,
      margin: [0, 2],
    };
  }
}
