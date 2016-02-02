$(document).ready(function () {
    $(".descriptionButton").click(function () {
        $(this).parents().next(".child").toggle("fast");
    });
});

