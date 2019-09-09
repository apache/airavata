console.log("Test.js is called here");

$('#gbLinks').on('click', function(e) {
    gb = $('#genomebuilder').val();
    var gbUrl = e.target.getAttribute('data-url');
    console.log(gbUrl + gb)
    window.open(gbUrl + gb);
    return false;
});
