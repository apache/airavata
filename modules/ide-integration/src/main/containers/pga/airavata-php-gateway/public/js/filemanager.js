var FILES;

// -------------------------------------------------- MESSAGE BOX
/**
 * add a message to the message box. context and type can be omitted.
 */
function add_msg(msg, context, type) {
    context = typeof context !== 'undefined' ? context : 'Info';
    type = typeof type !== 'undefined' ? type : 'alert-info';

    $('div#msgbox').html(
        $('<div />').addClass('alert').addClass(type).append(
            '<a class="close" data-dismiss="alert">&times;</a>',
            '<strong>' + context + ':</strong> ' + msg
        )
    );
}

// -------------------------------------------------- BROWSE
/**
 * set breadcrumb according to PATH.
 */
function set_breadcrumb() {
    // add parts
    var parts = PATH.split('/');
    var html = '';
    var link = '';
    for (var i = 0; i < parts.length; i++) {
        if (i != parts.length - 1) {
            link += i == 0 ? parts[i] : '/' + parts[i];
            html += '<li><a href="' + link + '">' + parts[i] + '</a></li>';
        } else {
            html += '<li class="active">' + parts[i] + '</li>';
        }
    }
    $('ol#breadcrumb').html(html);

    // register click event
    $('ol#breadcrumb a').click(function (e) {
        e.preventDefault();
        browse($(e.target).attr('href'));
    });
}

/**
 * do the ajax request for the new path.
 */
function browse(path) {
    $.ajax({
        url: "../files/get",
        cache: false,
        dataType: 'json',
        data:{ path:path},
        success: function (result) {
            if (result.status)
                show_content(path, result.files);
            else
                add_msg(result.msg, 'PHP', 'alert-danger');
        },
        error: function (jqXHR, status) {
            add_msg(status, 'AJAX', 'alert-danger');
        }
    });
}


/**
 * handles click on anchor to a directory
 * @param e
 * @param path
 */
function dir_click(path){
    browse(path);
    return false;
}

/**
 * ajax success callback, set path and add content to table.
 */
function show_content(path, files) {
    PATH = path;
    set_breadcrumb();
    $('#filter-text').val('');
    FILES = files;
    show_table(files);
}

function show_table(files){
    $('table#filemanager').empty();

    html = "";
    for (var i = 0; i < files.length; i++) {
        var f = files[i];
        var icon_class, name_link;
        if (f.folder) {
            icon_class = 'glyphicon glyphicon-folder-open';
            name_link = '<a href="#" onclick=dir_click(' + JSON.stringify(f.link) + ')>&nbsp;' + f.name + "</a>";
        } else {
            icon_class = 'glyphicon glyphicon-save-file';
            name_link = '<a href="../download?path=' + encodeURIComponent(f.link) + '">&nbsp;' + f.name + '</a>';
        }

        html += '<tr>'
            + '<td><span class="' + icon_class + '"></span> ' + name_link + '</td>'
            + '<td>' + f.size + '</td>'
            + '<td>' + f.date + '</td>'
            + '<td>' + f.perm + '</td>'
            + '</tr>';
    }
    $('table#filemanager').html(html);
}

$('div#tools a#refresh-button').click(function (e) {
    browse(PATH);
    $('#filter-text').val('');
});

$('div#tools a#clear-msgbox-button').click(function (e) {
    $('div#msgbox').empty();
    $('#filter-text').val('');
});

function file_filter(file){
    var text = $('#filter-text').val();
    if(file.link.indexOf(text) !== -1){
        return true;
    }
    return false;
}

$('#filter-text').on("keypress", function(e) {
    if (e.keyCode == 13) {
        var files = FILES.filter(file_filter);
        show_table(files);
        return false;
    }
});