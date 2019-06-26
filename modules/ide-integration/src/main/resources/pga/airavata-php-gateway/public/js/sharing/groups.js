/**
 * @author Jeff Kinnison <jkinniso@nd.edu>
 */
var dummy_user_data = [
    {
        username: 'testuser1',
        firstname: 'Jane',
        lastname: 'Doe',
        email: 'jadoe@institution.edu'
    },
    {
        username: 'testuser2',
        firstname: 'Ego',
        lastname: 'Id',
        email: 'freud@institution.gov'
    },
    {
        username: 'testuser3',
        firstname: 'Ivan',
        lastname: 'Ivanov',
        email: 'notkgb@totallynotkgb.ru'
    },
    {
        username: 'testuser4',
        firstname: 'Grok',
        lastname: 'Smytheson',
        email: 'popsicle@prehistoric.com'
    },
    {
        username: 'testuser5',
        firstname: 'Identifier',
        lastname: 'Appellation',
        email: 'idapp@institution.edu'
    }
];

$(function() {
    var comparator_map, comparator, $original_shared_list, $revoke_list;
    comparator_map = {
            "username": usernameComparator,
            "firstlast": firstLastComparator,
            "lastfirst": lastFirstComparator,
            "email": emailComparator
    };
    comparator = usernameComparator;

    var createTestData = function() {
        var $users, $user, data;

        $users = $('#share-box-users');

        for (var user in dummy_user_data) {
            if (dummy_user_data.hasOwnProperty(user)) {
                data = dummy_user_data[user];
                $user = createThumbnail(data.username, data.firstname, data.lastname, data.email, 0, false);
                $user.addClass('share-box-users-item');
                $users.append($user);
            }
        }
    }

    // Filter visible user thumbnails based on pattern in .share-box-filter
    $('body').on('keyup', '.share-box-filter', function (e) {
        var $target, pattern, visible, $users;
        e.preventDefault();
        e.stopPropagation();
        $target = $(e.target);
        pattern = $target.val().toLowerCase();
        if (!pattern || pattern === '') {
            pattern = /.+/;
        }
        $users = $('#share-box-users').children(visible);
        userFilter($users, pattern);
        return false;
    });

    // Toggle between visible groups and visible users
    // $('body').on('click', '.show-results-btn', function(e) {
    // 	var $target;
    // 	e.preventDefault();
    // 	e.stopPropagation();
    // 	$target = $(e.target);
    // 	if ($target.hasClass("show-groups") && !$target.hasClass('btn-primary')) {
    // 		$('.group-thumbnail').show();
    // 		$('.user-thumbnail').hide();
    // 		$('.show-groups').addClass('btn-primary');
    // 		$('.show-groups').removeClass('btn-default');
    // 		$('.show-users').addClass('btn-default');
    // 		$('.show-users').removeClass('btn-primary');
    // 	}
    // 	else if ($target.hasClass("show-users") && !$target.hasClass('btn-primary')) {
    // 		$('.user-thumbnail').show();
    // 		$('.group-thumbnail').hide();
    // 		$('.show-users').addClass('btn-primary');
    // 		$('.show-users').removeClass('btn-default');
    // 		$('.show-groups').addClass('btn-default');
    // 		$('.show-groups').removeClass('btn-primary');
    // 	}
    // 	return false;
    // });

    // Sort users by some metric
    $('body').on('change', '.order-results-selector', function(e) {
        var $target, $sibling, $sorted;
        $target = $(e.target);
        console.log($target);
        comparator = comparator_map[$target.val()];
        $('.order-results-selector').val($target.val());
        $sibling = $target.siblings('#share-box-users');
        $sorted = $sibling.children('.sharing-thumbnail');
        $sorted.detach();
        $sorted.sort(comparator);
        $sibling.append($sorted);
    });

    // Handle adding and removing membership
    $('body').on('click', '.share-box-users-item, .sharing-thumbnail-unshare', function(e) {
        var $target, $share;
        e.stopPropagation();
        e.preventDefault();
        console.log("Click!");
        $target = $(e.target).closest('.sharing-thumbnail');
        changeShareState($target);
        $share = $('#share-box-share');
        if ($share.children().length === 0) {
            $share.append('<p>No members yet</p>');
        }
        else {
            $par = $share.find('p');
            $par.remove();
        }
        return false;
    });

    $('body').on('change', '#share-box-share', function(e) {
        var $target, $par;
        e.preventDefault();
        e.stopPropagation();
        $target = $(e.target);
        if ($target.children().length === 0) {
            $target.append('<p>No members yet</p>');
        }
        else {
            $par = $target.find('p');
            $par.remove();
        }
        return false;
    });

    $('body').on('click', 'input[type="reset"]', function (e) {
        var $shared_users;
        $shared_users = $('.share-box-share-item');
        $shared_users.toggleClass('.share-box-share-item .share-box-users-item');
        $shared_users.find('.sharing-thumbnail-access').val(access_enum.NONE).hide();
        $shared_users.detach().appendTo('#share-box-users');
        $('.order-results-selector').trigger('change');
        $('#shared-users').addClass('text-align-center');
        $('#shared-users').prepend('<p>This project has not been shared</p>');
    });

    createTestData();
});
