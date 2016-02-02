$(document).ready(function () {
    $(".expandLink").click(function () {
        $(this).parents().next(".child").toggle("fast");
    });
});

