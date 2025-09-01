import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { finalize, Subject, takeUntil } from 'rxjs';
import { ReportDetails } from '../../../core/models/ReportDetails';
import * as XLSX from 'xlsx';
import { GlobalService } from '../../../core/services/global-service.service';
import { VulnerabilitiesSummaryService } from '../../../core/services/vulnerabilities-summary.service';
import { ReportDetailsService } from '../../../core/services/report-details.service';
import { ReportColumnType } from '../../../core/enums/ReportColumnType.enum';
import { VulnerabilitiesSummary } from '../../../core/models/VulnerabilitiesSummary';
import ExcelJS from 'exceljs';
import { SharedModule } from '../../../../shared/shared.module';
import { AssessmentEnum } from '../../../core/enums/Assessment.enum';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

type AOA = any[][];
@Component({
  selector: 'app-vulnerabilities-upload',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './vulnerabilities-upload.component.html',
  styleUrl: './vulnerabilities-upload.component.css',
})
export class VulnerabilitiesUploadComponent implements OnInit, OnDestroy {
  @ViewChild('content') content: any;
  data: AOA = [];
  wopts: XLSX.WritingOptions = { bookType: 'xlsx', type: 'array' };
  fileName: string = 'Vulnerability-Excel-Sample.xlsx';
  isLoading: boolean;
  showError: boolean;
  errorMsg: string;
  public destroy$: Subject<boolean>;
  columnLength: number;
  columnTypeEnum = ReportColumnType;
  assessmentEnum = AssessmentEnum;
  errorRows: any[];
  reportDetailsObj: ReportDetails;
  duplicateReportDetails: any;

  constructor(
    private globalService: GlobalService,
    public vulnerabilitiesSummaryService: VulnerabilitiesSummaryService,
    public reportDetailsService: ReportDetailsService,
    private spinner: NgxUiLoaderService,
    private modalService: NgbModal
  ) {
    this.isLoading = false;
    this.showError = false;
    this.errorMsg = '';
    this.destroy$ = new Subject<boolean>();
    this.columnLength = 17;
    this.errorRows = [];
    this.reportDetailsObj = {} as ReportDetails;
  }

  ngOnInit(): void {}

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  async onFileChange(evt: any) {
    this.errorRows = [];

    const target: DataTransfer = <DataTransfer>evt.target;
    if (target.files.length !== 1) {
      this.globalService.showAlert('Error', 'Cannot use multiple files');
      return;
    }

    const file = target.files[0];
    const { rows, imageMap } = await this.parseExcelWithImages(file);

    this.data = rows;

    if (this.data?.length < 2) {
      this.globalService.showAlert(
        'Error',
        'Please enter at least one report detail!'
      );
      return;
    }

    if (this.data[0].length !== this.columnLength) {
      this.globalService.showAlert('Error', 'Please check the column length!');
      return;
    }

    if (this.checkRowDataTypes(this.data)) {
      this.buildBody(imageMap);
    }
  }

  // checkRowDataTypes(data: any): boolean {
  //   let valid = true;
  //   const typeArray = Object.keys(this.columnTypeEnum);
  //   const typeArrayValues = Object.values(this.columnTypeEnum);

  //   data.forEach((row: any, rowNumber: number) => {
  //     if (rowNumber !== 0) {
  //       row.forEach((cellValue: any, colIndex: number) => {
  //         const colName = typeArray[colIndex];
  //         if (colName === 'header_Image' || colName === 'company_Logo') {
  //           return;
  //         }
  //         const expectedType = typeArrayValues[colIndex];
  //         if (typeof cellValue !== expectedType) {
  //           valid = false;
  //           this.errorRows.push({
  //             rowNumber: rowNumber + 1,
  //             vulnerability: row[typeArray.indexOf('vulnerability')],
  //             errorColName: colName,
  //             receivedInput: cellValue,
  //           });
  //         }
  //       });
  //     }
  //   });
  //   console.log('Final Payload from checkRowDataTypes :', this.errorRows);
  //   return valid;
  // }

  // buildBody(imageMap: Record<string, string>) {
  //   if (this.errorRows.length > 0) {
  //     this.globalService.showAlert('Error', 'Unable to generate data!');
  //     return;
  //   }

  //   const headerRow = this.data[0]; // first row is header

  //   const getColIndex = (key: keyof typeof ReportColumnType) =>
  //     headerRow.findIndex((h: any) => h?.toString().trim() === key);

  //   let reportDetails: ReportDetails | null = null;
  //   const vulnerabilities: VulnerabilitiesSummary[] = [];

  //   this.data.forEach((row: any, rowIndex: number) => {
  //     if (rowIndex === 0) return; // skip header row

  //     if (rowIndex === 1) {
  //       reportDetails = {
  //         typeOfTesting: this.checkUndefined(
  //           row[getColIndex('type_Of_Testing')]
  //         ),
  //         scope: this.checkUndefined(row[getColIndex('scope')]),
  //         // Here, check the imageMap for actual base64 data and set it if available
  //         headerImage:
  //           imageMap[`${rowIndex + 1}:${getColIndex('header_Image') + 1}`] ||
  //           '',
  //         headerText: this.checkUndefined(row[getColIndex('header_Text')]),
  //         footerText: this.checkUndefined(row[getColIndex('footer_Text')]),
  //         applicationName: this.checkUndefined(
  //           row[getColIndex('application_Name')]
  //         ),
  //         assessmentType: this.checkUndefined(
  //           Number(row[getColIndex('assessment_Type')]) === 0 ? 0 : 1
  //         ),
  //         companyName: this.checkUndefined(row[getColIndex('company_Name')]),
  //         // Similarly, check for company logo
  //         companyLogo:
  //           imageMap[`${rowIndex + 1}:${getColIndex('company_Logo') + 1}`] ||
  //           '',
  //         reportId: null,
  //         createdDate: Date.now(),
  //         updatedDate: Date.now(),
  //         vulnerabilities: [],
  //       };
  //     }

  //     const vulnerability: VulnerabilitiesSummary = {
  //       id: null,
  //       vulId: null,
  //       severity: this.checkUndefined(row[getColIndex('severity')]),
  //       vulnerability: this.checkUndefined(row[getColIndex('vulnerability')]),
  //       affectedScope: this.checkUndefined(row[getColIndex('affectedScope')]),
  //       description: this.checkUndefined(row[getColIndex('description')]),
  //       observation: this.checkUndefined(row[getColIndex('observation')]),
  //       testDetails: this.checkUndefined(row[getColIndex('testDetails')]),
  //       remediation: this.checkUndefined(row[getColIndex('remediation')]),
  //       references: this.checkUndefined(row[getColIndex('references')]),
  //       reportId: null,
  //       createdDate: Date.now(),
  //       updatedDate: Date.now(),
  //     };

  //     vulnerabilities.push(vulnerability);
  //   });

  //   if (reportDetails) {
  //     reportDetails.vulnerabilities = vulnerabilities;
  //     this.reportDetailsObj = reportDetails;

  //     console.log('Final Payload:', this.reportDetailsObj);
  //   }
  // }

  checkRowDataTypes(data: any): boolean {
    let valid = true;

    const typeArray = Object.keys(this.columnTypeEnum); // e.g. ['type_Of_Testing', ..., 'vulnerability']
    const typeArrayValues = Object.values(this.columnTypeEnum); // e.g. ['string', ..., 'string']

    data.forEach((row: any, rowNumber: number) => {
      if (rowNumber === 0) return; // skip header

      row.forEach((cellValue: any, colIndex: number) => {
        const colName = typeArray[colIndex];

        // Skip image columns
        if (colName === 'header_Image' || colName === 'company_Logo') {
          return;
        }

        // Only check full report fields in row 1
        const isReportField = [
          'type_Of_Testing',
          'scope',
          'header_Text',
          'footer_Text',
          'application_Name',
          'assessment_Type',
          'company_Name',
        ].includes(colName);

        if (isReportField && rowNumber > 1) {
          return; // skip report fields in later rows
        }

        const expectedType = typeArrayValues[colIndex];
        const actualType = typeof cellValue;

        if (
          colName === 'test_Details' &&(cellValue === null || cellValue === '')) {
          return;
        }

        if (colName === 'assessment_Type') {
          const validAssessmentTypes = Object.keys(this.assessmentEnum); // ['Authenticated', 'Unauthenticated']
          if (!validAssessmentTypes.includes(cellValue)) {
            valid = false;
            this.errorRows.push({
              rowNumber: rowNumber + 1,
              vulnerability: row[typeArray.indexOf('vulnerability')],
              errorColName: colName,
              receivedInput: cellValue,
            });
          }
          return;
        }

        if (actualType !== expectedType) {
          valid = false;
          this.errorRows.push({
            rowNumber: rowNumber + 1,
            vulnerability: row[typeArray.indexOf('vulnerability')],
            errorColName: colName,
            receivedInput: cellValue,
          });
        }
      });
    });

    console.log(this.errorRows);
    return valid;
  }

  buildBody(imageMap: Record<string, string[]>) {
    if (this.errorRows.length > 0) {
      this.globalService.showAlert('Error', 'Unable to generate data!');
      return;
    }

    const headerRow = this.data[0]; // First row is header

    const getColIndex = (key: keyof typeof ReportColumnType) =>
      headerRow.findIndex((h: any) => h?.toString().trim() === key);

    let reportDetails: ReportDetails | null = null;
    const vulnerabilities: VulnerabilitiesSummary[] = [];

    this.data.forEach((row: any, rowIndex: number) => {
      if (rowIndex === 0) return; // skip header row

      // Build ReportDetails from row 1
      if (rowIndex === 1) {
        reportDetails = {
          typeOfTesting: this.checkUndefined(
            row[getColIndex('type_Of_Testing')]
          ),
          scope: this.checkUndefined(row[getColIndex('scope')]),
          headerImage:
            imageMap[
              `${rowIndex + 1}:${getColIndex('header_Image') + 1}`
            ]?.[0] || '',
          headerText: this.checkUndefined(row[getColIndex('header_Text')]),
          footerText: this.checkUndefined(row[getColIndex('footer_Text')]),
          applicationName: this.checkUndefined(
            row[getColIndex('application_Name')]
          ),
          assessmentType: this.checkUndefined(
            this.assessmentEnum[row[getColIndex('assessment_Type')]] // Converts 'Authenticated' to 1
          ),
          companyName: this.checkUndefined(row[getColIndex('company_Name')]),
          companyLogo:
            imageMap[
              `${rowIndex + 1}:${getColIndex('company_Logo') + 1}`
            ]?.[0] || '',
          reportId: null,
          createdDate: Date.now(),
          updatedDate: Date.now(),
          vulnerabilities: [],
        };
      }

      const vulColIndex = getColIndex('vulnerability');
      if (vulColIndex !== -1 && row[vulColIndex]) {
        const vulnerability: VulnerabilitiesSummary = {
          id: null,
          vulId: null,
          severity: this.checkUndefined(row[getColIndex('severity')]),
          vulnerability: this.checkUndefined(row[getColIndex('vulnerability')]),
          affectedScope: this.checkUndefined(row[getColIndex('affectedScope')]),
          description: this.checkUndefined(row[getColIndex('description')]),
          observation: this.checkUndefined(row[getColIndex('observation')]),
          testDetails: this.getTestDetails(rowIndex, imageMap), // Concatenate all test details images
          remediation: this.checkUndefined(row[getColIndex('remediation')]),
          references: this.checkUndefined(row[getColIndex('references')]),
          reportId: null,
          createdDate: Date.now(),
          updatedDate: Date.now(),
        };

        vulnerabilities.push(vulnerability);
      }
    });

    if (reportDetails) {
      reportDetails.vulnerabilities = vulnerabilities;
      this.reportDetailsObj = reportDetails;

      console.log('Final Payload:', this.reportDetailsObj);
    }
  }

  getTestDetails(rowIndex: number, imageMap: Record<string, string[]>): string {
    const testDetailsImages: string[] = [];

    // Loop through imageMap and gather all images for this row's testDetails
    Object.keys(imageMap).forEach((cellKey) => {
      if (cellKey.startsWith(`${rowIndex + 1}:`)) {
        // If the image belongs to the current row, add the <img> tag for each image
        imageMap[cellKey].forEach((imageBase64) => {
          testDetailsImages.push(
            `<img src="${imageBase64}" style="max-width:100%; margin: 4px;" />`
          );
        });
      }
    });

    // Join all <img> tags into a single string and return it
    return testDetailsImages.join('');
  }

  checkUndefined(value: any): any {
    return value === undefined || value === null || value === '' ? null : value;
  }

  isValidAssessment(value: string): boolean {
    return value in this.assessmentEnum;
  }

  async parseExcelWithImages(
    file: File
  ): Promise<{ rows: any[]; imageMap: Record<string, string[]> }> {
    const workbook = new ExcelJS.Workbook();
    await workbook.xlsx.load(await file.arrayBuffer());
    const worksheet = workbook.getWorksheet(1);

    const rows: any[] = [];

    worksheet.eachRow((row, rowNumber) => {
      const rowData: any[] = [];
      for (let colIndex = 1; colIndex <= worksheet.columnCount; colIndex++) {
        const cell = row.getCell(colIndex);
        rowData.push(cell.value);
      }
      rows.push(rowData);
    });

    const imageMap: Record<string, string[]> = {}; // Store images per cell as arrays

    const media = workbook.model.media || [];

    // Loop through images in worksheet
    worksheet.getImages().forEach((img) => {
      const { tl } = img.range; // top-left cell
      const cellKey = `${tl.nativeRow + 1}:${tl.nativeCol + 1}`; // e.g. "2:2" = row 2 col 2 (B2)

      const image = media[img.imageId];
      if (image && image.buffer) {
        const base64 = `data:image/${image.type};base64,${image.buffer.toString(
          'base64'
        )}`;

        // Store image in imageMap for the specific cell
        if (!imageMap[cellKey]) {
          imageMap[cellKey] = [];
        }
        imageMap[cellKey].push(base64); // Add each image to the array
      }
    });

    return { rows, imageMap };
  }

  // async parseExcelWithImages(
  //   file: File
  // ): Promise<{ rows: any[]; imageMap: Record<string, string> }> {
  //   const workbook = new ExcelJS.Workbook();
  //   await workbook.xlsx.load(await file.arrayBuffer());
  //   const worksheet = workbook.getWorksheet(1);

  //   const rows: any[] = [];

  //   worksheet.eachRow((row, rowNumber) => {
  //     const rowData: any[] = [];
  //     for (let colIndex = 1; colIndex <= worksheet.columnCount; colIndex++) {
  //       const cell = row.getCell(colIndex);
  //       rowData.push(cell.value);
  //     }
  //     rows.push(rowData);
  //   });

  //   const imageMap: Record<string, string> = {};
  //   const media = workbook.model.media || [];

  //   // Loop through images in worksheet
  //   worksheet.getImages().forEach((img) => {
  //     const { tl } = img.range; // top-left cell
  //     const cellKey = `${tl.nativeRow + 1}:${tl.nativeCol + 1}`; // e.g. "2:2" = row 2 col 2 (B2)

  //     const image = media[img.imageId];
  //     if (image && image.buffer) {
  //       const base64 = `data:image/${image.type};base64,${image.buffer.toString(
  //         'base64'
  //       )}`;
  //       imageMap[cellKey] = base64;
  //     }
  //   });

  //   return { rows, imageMap };
  // }

  // callService() {
  //   if (this.errorRows.length > 0) {
  //     this.globalService.showAlert('Error', 'Unable to generate the call !');
  //     return;
  //   }
  //   this.spinner.start();
  //   this.reportDetailsService
  //     .addReportDetailsVulnerabilities(this.reportDetailsObj)
  //     .pipe(
  //       takeUntil(this.destroy$),
  //       finalize(() => {
  //         this.spinner.stop();
  //       })
  //     )
  //     .subscribe({
  //       next: (data: ReportDetails) => {
  //         console.log(data);
  //         if (data) {
  //           if (data && data.duplicateVulnerabilities && data.duplicateVulnerabilities.length > 0) {
  //             this.globalService.showAlert(
  //               'Success',
  //               'Vulnerability list data uploaded successfully !'
  //             );
  //           } else {
  //             this.globalService.showAlert(
  //               'Error',
  //               'Please check the duplications & data must be valid !'
  //             );
  //             (data.duplicateVulnerabilities || []).forEach((element) => {
  //               this.errorRows.push({
  //                 rowNumber: '',
  //                 reportId: element.reportId,
  //                 errorColName: '',
  //                 receivedInput: '',
  //               });
  //             });
  //           }
  //         } else {
  //           this.showError = true;
  //           this.errorMsg = 'Unable to update Candidate !';
  //           this.globalService.showAlert(
  //             'Error',
  //             'Unable to upload Vulnerability list data !'
  //           );
  //         }
  //       },
  //       error: (e) => {
  //         console.error(e);
  //         this.showError = true;
  //         let msg = e?.error?.message
  //           ? e?.error?.message
  //           : 'Error in uploading Vulnerability list data !';
  //         msg = this.globalService.beautifyErrorMsg(msg);
  //         this.errorMsg = msg;
  //         this.globalService.showAlert('Error', msg);
  //       },
  //       complete: () => console.info('complete'),
  //     });
  // }

  callService() {
    if (this.errorRows.length > 0) {
      this.globalService.showAlert('Error', 'Unable to generate the call!');
      return;
    }
    this.spinner.start();
    this.reportDetailsService
      .checkScope(this.reportDetailsObj.scope)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.spinner.stop();
        })
      )
      .subscribe({
        next: (scopeResponse) => {
          if (scopeResponse.isDuplicate) {
            const reportId = scopeResponse.reportId;
            this.reportDetailsService
              .getReport(reportId)
              .pipe(
                takeUntil(this.destroy$),
                finalize(() => {
                  this.spinner.stop();
                })
              )
              .subscribe({
                next: (reportDetails) => {
                  this.duplicateReportDetails = reportDetails;
                  this.modalService.open(this.content);
                },
                error: (e) => {
                  this.showError = true;
                  let msg = e?.error?.message
                    ? e?.error?.message
                    : 'Error fetching existing report details!';
                  msg = this.globalService.beautifyErrorMsg(msg);
                  this.errorMsg = msg;
                  this.globalService.showAlert('Error', msg);
                },
              });
          } else {
            this.uploadVulnerabilities();
          }
        },
        error: (error) => {
          this.showError = true;
          let msg = error?.error?.message
            ? error?.error?.message
            : 'Error checking scope';
          msg = this.globalService.beautifyErrorMsg(msg);
          this.errorMsg = msg;
          this.globalService.showAlert('Error', msg);
          this.spinner.stop();
        },
      });
  }

  continueWithExistingReport() {
    this.closeModal(),
      (this.reportDetailsObj.reportId = this.duplicateReportDetails.reportId);
    this.reportDetailsService
      .addReportDetailsVulnerabilitiesFExcel(this.reportDetailsObj)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.spinner.stop();
        })
      )
      .subscribe({
        next: (data) => {
          console.log('Vulnerabilities added successfully', data);
          this.globalService.showAlert(
            'Success',
            'Vulnerabilities uploaded successfully to existing report!'
          );
        },
        error: (e) => {
          console.error('Error uploading vulnerabilities:', e);
          this.showError = true;
          let msg = e?.error?.message
            ? e?.error?.message
            : 'Error uploading vulnerabilities!';
          msg = this.globalService.beautifyErrorMsg(msg);
          this.errorMsg = msg;
          this.globalService.showAlert('Error', msg);
        },
      });
  }

  // continueUpload() {
  //   // If the user clicks continue, proceed with uploading vulnerabilities to the existing report
  //   if (this.duplicateReportDetails) {
  //     this.reportDetailsObj.reportId = this.duplicateReportDetails.reportId; // Use the existing report ID
  //     this.uploadVulnerabilities();
  //     this.closeModal();
  //   }
  // }

  stopUpload() {
    this.globalService.showAlert(
      'Stopped',
      'Uploading vulnerabilities has been canceled.'
    );
    this.closeModal();
  }

  closeModal() {
    this.modalService.dismissAll();
  }

  uploadVulnerabilities() {
    if (
      !this.reportDetailsObj.vulnerabilities ||
      this.reportDetailsObj.vulnerabilities.length === 0
    ) {
      this.globalService.showAlert('Error', 'No vulnerabilities to upload.');
      return;
    }
    this.reportDetailsService
      .addReportDetailsVulnerabilitiesFExcel(this.reportDetailsObj)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.spinner.stop();
        })
      )
      .subscribe({
        next: (response) => {
          console.log('Vulnerabilities uploaded successfully:', response);
          this.globalService.showAlert(
            'Success',
            'Vulnerabilities uploaded successfully.'
          );
        },
        error: (e) => {
          console.error('Error uploading vulnerabilities:', e);
          this.showError = true;
          let msg = e?.error?.message
            ? e?.error?.message
            : 'Error uploading vulnerabilities.';
          msg = this.globalService.beautifyErrorMsg(msg);
          this.errorMsg = msg;
          this.globalService.showAlert('Error', msg);
        },
      });
  }

  sampleDownload(): void {
    const typeArray = Object.keys(this.columnTypeEnum);
    let data = [typeArray, []];
    /* generate worksheet */
    const ws: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(data);

    /* generate workbook and add the worksheet */
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Vulnerability Excel Sample');

    /* save to file */
    XLSX.writeFile(wb, this.fileName);
  }
}
