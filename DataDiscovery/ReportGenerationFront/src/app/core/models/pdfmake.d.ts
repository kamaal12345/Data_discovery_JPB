declare module 'pdfmake/build/pdfmake' {
  const pdfMake: any;
  export = pdfMake;
}

declare module 'pdfmake/build/vfs_fonts' {
  export const pdfMake: { vfs: { [key: string]: string } };
}
