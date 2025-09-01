import { Injectable } from '@angular/core';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { TDocumentDefinitions } from 'pdfmake/interfaces';

@Injectable({ providedIn: 'root' })
export class PDFService {
  private pdfMake: any;

  constructor(private spinner: NgxUiLoaderService) {}

  private async loadPDFMaker() {
    if (!this.pdfMake) {
      const pdfMakeModule = await import('pdfmake/build/pdfmake');
      const pdfFonts = await import('pdfmake/build/vfs_fonts');

      pdfMakeModule.vfs =
        (pdfFonts as any).vfs || (pdfFonts as any).pdfMake?.vfs;

      this.pdfMake = pdfMakeModule;
    }
  }

  // async open(def: TDocumentDefinitions) {
  //   try {
  //     this.spinner.start();
  //     await this.loadPDFMaker();
  //     this.pdfMake.createPdf(def).open();
  //   } catch (error) {
  //     console.error('Failed to load pdfmake lib:', error);
  //   } finally {
  //     this.spinner.stop();
  //   }
  // }

  async open(def: TDocumentDefinitions) {
    try {
      this.spinner.start();
      await this.loadPDFMaker();

      // Generate PDF blob first
      const pdfBlob = await new Promise<Blob>((resolve, reject) => {
        this.pdfMake.createPdf(def).getBlob((blob: Blob) => {
          blob ? resolve(blob) : reject(new Error('PDF generation failed'));
        });
      });

      // Create Blob URL and open it
      const blobUrl = URL.createObjectURL(pdfBlob);
      window.open(blobUrl);

      // Optional: Clean up after a while (avoid memory leaks)
      setTimeout(() => {
        URL.revokeObjectURL(blobUrl);
      }, 60000); // 1 minute
    } catch (error) {
      console.error('PDF load error:', error);
    } finally {
      this.spinner.stop();
    }
  }

  async generateBuffer(def: TDocumentDefinitions): Promise<Uint8Array> {
    try {
      this.spinner.start();
      await this.loadPDFMaker();

      return new Promise<Uint8Array>((resolve, reject) => {
        this.pdfMake.createPdf(def).getBuffer((buffer: Uint8Array) => {
          if (buffer) {
            resolve(buffer);
          } else {
            reject(new Error('Failed to generate PDF buffer'));
          }
        });
      });
    } catch (error) {
      console.error('Failed to generate PDF buffer:', error);
      throw error;
    } finally {
      this.spinner.stop();
    }
  }

  // Save the generated PDF buffer as a file (optional, if needed)
  async savePdf(def: TDocumentDefinitions, fileName: string) {
    try {
      this.spinner.start();
      const buffer = await this.generateBuffer(def);

      const blob = new Blob([buffer], { type: 'application/pdf' });
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = fileName;
      link.click();
    } catch (error) {
      console.error('Failed to save PDF:', error);
    } finally {
      this.spinner.stop();
    }
  }
}
