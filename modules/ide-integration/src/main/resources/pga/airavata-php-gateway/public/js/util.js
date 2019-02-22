
var util = (function(){
    "use strict";

    return {
        sanitizeHTMLId: function(id) {
            // Replace anything that isn't an HTML safe id character with underscore
            // Here safe means allowable by HTML5 and also safe to use in a jQuery selector
            return id.replace(/[^a-zA-Z0-9_-]/g, "_");
        },

        /**
         * Return a list of filenames that exceed the given maxUploadFileSize.
         *
         * @param fileList is of type FileList
         * @param maxUploadFileSize is a number of megabytes
         * @return Array of string filenames. Array is empty if none of the
         * files exceed the given maxUploadFileSize.
         */
        validateMaxUploadFileSize: function(fileList, maxUploadFileSize) {

            var tooLargeFilenames = [];
            for (var i=0; i < fileList.length; i++) {

                var file = fileList[i];
                var inputFileSize = file.size / (1024 * 1024);
                if (inputFileSize > maxUploadFileSize) {
                    tooLargeFilenames.push(file.name);
                }
            }

            return tooLargeFilenames;
        }
    };
})();
