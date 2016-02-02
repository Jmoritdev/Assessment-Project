function getUrlParam(paramName) {
    var reParam = new RegExp('(?:[\?&]|&)' + paramName + '=([^&]+)', 'i');
    var match = window.location.search.match(reParam);

    return (match && match.length > 1) ? match[1] : null;
}

function returnFileUrl(url) {
    var funcNum = getUrlParam("CKEditorFuncNum");
    var fileUrl = url;
    window.opener.CKEDITOR.tools.callFunction(funcNum, fileUrl);
    window.close();
}

