export default {
    editable: true,
    spellcheck: true,
    height: '15rem',
    minHeight: '5rem',
    placeholder: 'Enter text here...',
    translate: 'no',
    defaultParagraphSeparator: 'p',
    defaultFontName: 'Arial',
    toolbarHiddenButtons: [
        // [
        //     'bold'
        // ],
        // [
        //     'insertImage',
        //     'insertVideo'
        // ]
    ],
    customClasses: [
        {
            name: 'quote',
            class: 'quote',
        },
        {
            name: 'redText',
            class: 'redText',
        },
        {
            name: 'titleText',
            class: 'titleText',
            tag: 'h1',
        },
    ],
};