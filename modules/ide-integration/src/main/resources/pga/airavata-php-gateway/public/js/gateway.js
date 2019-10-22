$(document).ready(function () {

    //show options on hovering on a gateway
		$(".panel-title").hover( 
			function(){
				$(this).find(".gateway-options").addClass("in");
			},
			function(){
				$(this).find(".gateway-options").removeClass("in");
			}
		);

		//search Gateway Profiles 
		$('.filterinput').keyup(function() {
	        var a = $(this).val();
	        if (a.length > 0) {
	            children = ($("#accordion1").children());

	            var containing = children.filter(function () {
	                var regex = new RegExp(a, 'i');
	                return regex.test($('a', this).text());
	            }).slideDown();
	            children.not(containing).slideUp();
	        } else {
	            children.slideDown();
	        }
	        return false;
	    });

	    //remove Compute Resource
	    $("body").on("click", ".remove-cr", function(){
			$(this).parent().parent().parent().remove();
			$(this).parent().parent().parent().find(".cr-pref-space").html("");
		});

		//remove Storage Resource
	    $("body").on("click", ".remove-cr", function(){
			$(this).parent().parent().parent().remove();
			$(this).parent().parent().parent().find(".sr-pref-space").html("");
		});


		$(".add-cr").click( function(){

			$(".add-compute-resource-block").find("#gatewayId").val( $(this).data("gpid"));
			$(this).after( $(".add-compute-resource-block").html() );
		});

		$(".add-dsp").click( function(){

			$(".add-data-storage-preference-block").find("#gatewayId").val( $(this).data("gpid"));
			$(this).after( $(".add-data-storage-preference-block").html() );
		});

		$("body").on("change", ".cr-select", function(){
			crId = $(this).val();
			//This is done as Jquery creates problems when using period(.) in id or class.
			crId = crId.replace(/\./g,"_");
            $(".cr-pref-space").html($("#cr-" + crId).html());
        });

		$("body").on("change", ".sr-select", function(){
			srId = $(this).val();
			//This is done as Jquery creates problems when using period(.) in id or class.
			srId = srId.replace(/\./g,"_");
			$(".sr-pref-space").html($("#sr-" + srId).html());
		});

		$(".edit-gateway").click( function(){
			$(".edit-gp-name").val( $(this).data("gp-name") );
			$(".edit-gp-desc").val( $(this).data("gp-desc") );
			$(".edit-gpId").val( $(this).data("gp-id") );
		});

		$(".delete-gateway").click( function(){
			$(".delete-gp-name").html( $(this).data("gp-name") );
			$(".delete-gpId").val( $(this).data("gp-id") );
		});

		$(".remove-compute-resource").click( function(){
			$(".remove-cr-name").html( $(this).data("cr-name") );
			$(".remove-crId").val( $(this).data("cr-id") );
			$(".cr-gpId").val( $(this).data("gp-id") );
		});

		$(".remove-storage-resource").click( function(){
			$(".remove-sr-name").html( $(this).data("sr-name") );
			$(".remove-srId").val( $(this).data("sr-id") );
			$(".sr-gpId").val( $(this).data("gp-id") );
		});
});