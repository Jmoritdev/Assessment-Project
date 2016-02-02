$("editModal").on("show.bs.modal", function (event) {
    var link = $(event.relatedTarget);
    var recipient = link.data("category");
    
    var modal = $(this);
    modal.find("modal-body input").val(recipient);
});

