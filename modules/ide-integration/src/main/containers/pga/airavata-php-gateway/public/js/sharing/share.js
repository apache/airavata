/**
 * Utilities for sharing projects and experiments between users
 *
 * @author Jeff Kinnison <jkinniso@nd.edu>
 */

var createThumbnails;

$(function() {
    var comparator_map, comparator, $original_shared_list, $revoke_list, share_settings,
        showSharingModal, hideSharingModal;
    comparator_map = {
            "username": usernameComparator,
            "firstlast": firstLastComparator,
            "lastfirst": lastFirstComparator,
            "email": emailComparator
    };
    comparator = usernameComparator;

    /* Share box functions */

    createThumbnails = function () {
        var $users, $share, $user;

        $users = $('#share-box-users');
        $share = $('#shared-users');

        share_settings = {};

        for (var user in users) {
            if (users.hasOwnProperty(user)) {
                var data = users[user];
                var access = access_enum.NONE;
                if (data.hasOwnProperty("access")) {
                    //console.log("Found access parameter");
                    if (data.access.write) {
                        access = access_enum.WRITE;
                    }
                    else if (data.access.read) {
                        access = access_enum.READ;
                    }
                }

                $user = createThumbnail(user, data.firstname, data.lastname, data.email, access, true);
                $user.find('.sharing-thumbnail-access').hide();

                $user.addClass('user-thumbnail');
                if (access === access_enum.NONE) {
                    $user.addClass('share-box-users-item');
                    $users.append($user);
                }
                else {
                    //console.log("adding shared user");
                    $user.addClass('share-box-share-item sharing-updated');
                    share_settings[user] = data.access;
                    $share.append($user);
                }
            }
        }

        for (var o in owner) {
            if (owner.hasOwnProperty(o)) {
                var odata = owner[o];
                $owner = createThumbnail(o, odata.firstname, odata.lastname, odata.email, access_enum.OWNER, false);
                $owner.find(".sharing-thumbnail-unshare").detach();
                $owner.addClass("share-box-share-item owner");
                $share.prepend($owner);
            }
        }

        // projectOwner is only present for experiment sharing
        if (typeof projectOwner !== 'undefined') {

            for (var po in projectOwner) {
                if (projectOwner.hasOwnProperty(po)) {
                    var podata = projectOwner[po];
                    $projectOwner = createThumbnail(po, podata.firstname, podata.lastname, podata.email, access_enum.PROJECT_OWNER, false);
                    $projectOwner.find(".sharing-thumbnail-unshare").detach();
                    $projectOwner.addClass("share-box-share-item owner");
                    $share.prepend($projectOwner);
                }
            }
        }

        if ($share.children().length === 0) {
            $share.append($('<p>This has not been shared</p>')).addClass('text-align-center');
        }
        $('#share-settings').val(JSON.stringify(share_settings));
        $('.user-thumbnail').show();
        $('.order-results-selector').trigger('change');
        //$('.group-thumbnail').show();
    };

    // Dispatch hide/show events when modal hides/shows
    showSharingModal = function() {
        $('#share-box').animate({top: "1%"}).trigger("show");
    };
    hideSharingModal = function() {
        $('#share-box').animate({top: '100%'}).trigger("hide");
    };


    /* Share box event handlers */

    // Create, populate, and show the share box
    $('body').on('click', 'button#entity-share, button#update-sharing', function(e) {
        var $share_list, ajax_data;
        e.stopPropagation();
        e.preventDefault();

        if ($('#share-box-users').find('.user-thumbnail').length === 0) {
            ajax_data = $(e.target).data();

            $('#share-box-users').addClass('text-align-center').text('Loading user list');

            // Block the whole modal while user list is loading. Reason: if user
            // tries to remove a shared user, loading of user list currently
            // overwrites that information.
            $('#share-box .modal-dialog').addClass('modal-spinner');
            $.ajax({
                url: ajax_data.url,
                method: 'get',
                data: ajax_data.resourceId ? {resourceId: ajax_data.resourceId} : null,
                dataType: "json",
                error: function(xhr, status, error) {
                    $('#shared-users').addClass('text-align-center').text("Unable to load users from Airavata server.");
                },
                success: function(data, status, xhr) {
                    var user, $user, $users;

                    $users = $('#share-box-users');
                    $users.removeClass('text-align-center');
                    $users.text('');
                    for (user in data) {
                        if (data.hasOwnProperty(user)) {
                            $user = createThumbnail(user, data[user].firstname, data[user].lastname, data[user].email, access_enum.NONE, true);
                            $user.find('.sharing-thumbnail-access').hide();

                            $user.addClass('user-thumbnail');
                            $user.addClass('share-box-users-item');
                            $users.append($user);
                        }
                    }
                },
                complete: function(){

                    $('#share-box .modal-dialog').removeClass('modal-spinner');
                }
            });
        }

        $share_list = $('#shared-users').children();
        if ($share_list.filter('.sharing-thumbnail').length > 0) {
            $share_list.sort(comparator);
            $share_list.each(function(index, element) {
                var $e;
                $e = $(element);
                if (!$e.hasClass('owner')) {
                    $e.find('.sharing-thumbnail-access-text').hide();
                }
                $e.find('.sharing-thumbnail-access').prop('disabled', false).show();
                $e.find('.sharing-thumbnail-unshare').show();
                $e.detach().appendTo($('#share-box-share'));
            });
        }
        $original_shared_list = $('#share-box-share').children();
        showSharingModal();
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
        $('#shared-users').prepend('<p>This has not been shared</p>');
    });

    // Filter the list as the user types
    $('body').on('keyup', '#share-box-filter', function(e) {
        var $target, pattern, visible, $users;
        e.stopPropagation();
        e.preventDefault();
        $target = $(e.target);
        pattern = $target.val().toLowerCase();
        if (!pattern || pattern === '') {
            pattern = /.+/;
        }
        visible = ($('.show-groups').hasClass('btn-primary') ? '.group-thumbnail' : '.user-thumbnail');
        $users = $('#share-box-users').children(visible);
        userFilter($users, pattern);
        return false;
    });

    $('body').on('click', '.show-results-btn', function(e) {
        var $target;
        e.preventDefault();
        e.stopPropagation();
        $target = $(e.target);
        if ($target.hasClass("show-groups") && !$target.hasClass('btn-primary')) {
            $('.group-thumbnail').show();
            $('.user-thumbnail').hide();
            $('.show-groups').addClass('btn-primary');
            $('.show-groups').removeClass('btn-default');
            $('.show-users').addClass('btn-default');
            $('.show-users').removeClass('btn-primary');
        }
        else if ($target.hasClass("show-users") && !$target.hasClass('btn-primary')) {
            $('.user-thumbnail').show();
            $('.group-thumbnail').hide();
            $('.show-users').addClass('btn-primary');
            $('.show-users').removeClass('btn-default');
            $('.show-groups').addClass('btn-default');
            $('.show-groups').removeClass('btn-primary');
        }
        return false;
    });

    $('body').on('change', '.order-results-selector', function(e) {
        var $target, $sibling, $sorted;
        $target = $(e.target);
        comparator = comparator_map[$target.val()];
        $('.order-results-selector').val($target.val());
        $sibling = $target.siblings('#shared-users, #share-box-users');
        $sorted = $sibling.children('.sharing-thumbnail');
        $sorted.detach();
        $sorted.sort(comparator);
        $sibling.append($sorted);
    });

    // Save the sharing permissions of each selected user
    $('body').on('click', '#share-box-button', function(e) {
        var data, resource_id, $share_list, $update_list, new_share_settings;
        e.stopPropagation();
        e.preventDefault();
        $('#share-box-error-message').empty();
        data = $("#share-box").data();
        $share_list = $("#share-box-share").children();
        $update_list = $('.sharing-to-update');
        // Clone current share settings
        new_share_settings = JSON.parse(JSON.stringify(share_settings));
        // TODO: is this used any longer?  I don't see where resource_id gets
        // set and updateUserPrivileges doesn't seem to be defined
        if (data.hasOwnProperty('resource_id')) {
            resource_id = data.resource_id;
            updateUserPrivileges(resource_id, $share_list);
        }
        else {
            if ($update_list.length > 0) {
                $update_list.each(function(index, element) {
                    var $e, data, newaccess;
                    $e = $(element);
                    data = $e.data();
                    newaccess = data.access;
                    if (data.hasOwnProperty('currentaccess')) {
                        newaccess = data.currentaccess;
                    }
                    new_share_settings[data.username] = newaccess;
                });
                if ($(this).data().hasOwnProperty('ajaxUpdateUrl')) {
                    ajaxUpdateSharing($(this).data().ajaxUpdateUrl, new_share_settings, function(){
                        updateSharingAndCloseModal(new_share_settings);
                    });
                } else {
                    updateSharingAndCloseModal(new_share_settings);
                }
            } else {
                updateSharingAndCloseModal(new_share_settings);
            }
        }
        return false;
    });

    var updateSharingAndCloseModal = function(new_share_settings) {

        var $share_list, $update_list;

        $share_list = $("#share-box-share").children();
        $update_list = $('.sharing-to-update');
        $('#shared-users').empty();
        if ($update_list.length > 0) {
            $share_list.sort(comparator_map.username);
            $update_list.each(function(index, element) {
                var $e, data;
                $e = $(element);
                data = $e.data();
                data.access = new_share_settings[data.username];
            });
            $('#share-settings').val(JSON.stringify(new_share_settings));
            share_settings = new_share_settings;
            $('#shared-users').removeClass('text-align-center');
        }
        if ($share_list.length === 0) {
            $('#shared-users').addClass('text-align-center');
            $('#shared-users').prepend('<p>This has not been shared</p>');
        }
        else {
            $share_list.each(function(index, element) {
                var $e, access;
                $e = $(element);
                access = parseInt($e.find('.sharing-thumbnail-access').prop('disabled', true).hide().val(), 10);
                $e.find('.sharing-thumbnail-access-text').text(access_text[access]).show();
                $e.find('.sharing-thumbnail-unshare').hide();
            });
            $share_list.detach().appendTo($('#shared-users'));
        }
        hideSharingModal();
        $update_list.removeClass('sharing-to-update');
        $update_list.addClass('updated');
    };

    var ajaxUpdateSharing = function(url, share_settings, callback) {
        $('#share-box .modal-dialog').addClass('modal-spinner');
        $.ajax({
            url: url,
            method: 'post',
            data: JSON.stringify(share_settings),
            contentType: 'application/json',
            dataType: "json",
            success: function(data, status, xhr) {
                if (data.success) {
                    callback();
                    $(    '<div class="alert alert-success fade in">'
                        +   '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>'
                        +   'Sharing settings updated successfully'
                        + '</div>'
                    ).appendTo('#shared-users-updated-message').alert().each(function(){
                        var alert = this;
                        window.setTimeout(function(){
                            $(alert).alert('close');
                        }, 5000);
                    });
                } else {
                    $(    '<div id="share-box-error-alert" class="alert alert-danger">'
                        +   data.error
                        + '</div>'
                    ).appendTo('#share-box-error-message');
                }
            },
            error: function(xhr, status, error) {
                console.log("Error while saving sharing settings", url, share_settings, status, error);
                $(    '<div id="share-box-error-alert" class="alert alert-danger">'
                    +   'Error occurred: ' + status
                    + '</div>'
                ).appendTo('#share-box-error-message');
            },
            complete: function(xhr, status) {
                $('#share-box .modal-dialog').removeClass('modal-spinner');
            }
        });
    };

    // Close the share box
    $('body').on('click', '#share-box-close, #share-box-x', function(e) {
        e.stopPropagation();
        e.preventDefault();
        $('#share-box-error-message').empty();
        $('#shared-users').empty();
        if ($original_shared_list.length > 0) {
            $original_shared_list.each(function(index, element) {
                var $e, data, access;
                $e = $(element);
                data = $e.data();
                if (data.hasOwnProperty('currentaccess')) {
                    data.currentaccess = data.access;
                }
                access = (data.access.write ? access_enum.WRITE : access_enum.READ);
                $e.find('.sharing-thumbnail-access').val(access).prop('disabled', true).hide();
                $e.find('.sharing-thumbnail-access-text').text(access_text[access]).show();
                $e.find('.sharing-thumbnail-unshare').hide();
                $e.removeClass('sharing-to-update');
            });
            $('#shared-users').removeClass('text-align-center');
            $original_shared_list.detach().appendTo('#shared-users');
        }
        else {
            $('#shared-users').addClass('text-align-center');
            $('#shared-users').prepend('<p>This has not been shared</p>');
        }
        $('.sharing-to-update').detach().appendTo($('#share-box-users'));
        $('.sharing-to-update').find('.sharing-thumbnail-access').val(access_enum.NONE).prop('disabled', true).hide();
        $('.sharing-to-update').find('.sharing-thumbnail-access-text').text(access_text[access_enum.NONE]).show();
        $('.sharing-to-update').find('.sharing-thumbnail-unshare').hide();
        $('.sharing-to-update').addClass('share-box-users-item').removeClass('sharing-to-update share-box-share-item');
        hideSharingModal();
        $('.order-results-selector').trigger('change');
        return false;
    });

    // Handle sharing and unsharing
    $('body').on('click', '.share-box-users-item, .sharing-thumbnail-unshare', function(e) {
        var $target;
        e.stopPropagation();
        e.preventDefault();
        $target = $(e.target).closest('.sharing-thumbnail');
        changeShareState($target);
        // if ($target.closest('ul, div').hasClass('share-box-share')) {
        //     $target.find('.sharing-thumbnail-access-text').hide();
        //     $target.find('.sharing-thumbnail-access').show();
        // }
        // else {
        //     $target.find('.sharing-thumbnail-access').hide();
        //     $target.find('.sharing-thumbnail-access-text').show();
        // }
        $('.share-box-filter').trigger('keydown');
        $('.order-results-selector').trigger('change');
        return false;
    });

    // Handle changing access level
    $('body').on('change', '.sharing-thumbnail-access', function(e) {
        var $target, $parent, data, access;
        $target = $(e.target);
        $parent = $target.closest('.sharing-thumbnail');
        data = $parent.data();
        access = parseInt($target.val());
        switch(access) {
            case 1:
                data.currentaccess.read = true;
                data.currentaccess.write = false;
                break;
            case 2:
                data.currentaccess.read = true;
                data.currentaccess.write = true;
                break;
            default:
                data.currentaccess.read = false;
                data.currentaccess.write = false;
        }
        $parent.find('.sharing-thumbnail-access-text').val(access_text[access]);
        $parent.data(data);
        $parent.addClass('sharing-to-update');
    });





    /* Set up the sharing interface */
    createThumbnails();
});
