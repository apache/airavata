<?php

class GroupController extends BaseController {
    public function __construct()
    {
        $this->beforeFilter('verifylogin');
        $this->beforeFilter('verifyauthorizeduser');
        Session::put("nav-active", "group");
    }

    public function createView()
    {
        return View::make("group/create");
    }

    public function createSubmit()
    {
        // TODO: Get the group name and description
        // TODO: Create the new group
        // TODO: Get users to add
        // TODO: Update membership of users in list
        if (isset($_POST['save'])) {
            $groupId = GroupUtilities::create_group();
            return Redirect::to('group/summary?groupId=' . $groupId);
        }
        else {
            return Redirect::to('group/create');
        }
    }

    public function editSubmit()
    {
        // TODO: Get users to edit
        // TODO: Update membership of users in list
    }

    public function summaryView()
    {
        // TODO: Determine if the user is a member of the group
        // TODO: Determine if the user is owner of the group
        // TODO: If not a member, load a page that says they cannot see the group
        // TODO: If a standard member, display group name, description, members, projects(?) and experiments (?)
        // TODO: If owner, display buttons to allow adding and removing members
        if (Input::has('groupId')) {
            Session::put('groupId', Input::get('groupId'));
            return View::make('group/summary', array('groupId' => $groupId));
        }
        else {
            return Redirect::to('home');
        }
    }
}

?>
