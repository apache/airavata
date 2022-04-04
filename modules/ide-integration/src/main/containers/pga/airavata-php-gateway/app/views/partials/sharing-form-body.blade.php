<label>Click on the users you would like to share with.</label>
<input id="share-box-filter" class="form-control" type="text" placeholder="Filter the user list" />
<!-- <label>Show</label>
<div id="show-results-group" class="btn-group" role="group" aria-label="Show Groups or Users">
    <button type="button" class="show-groups show-results-btn btn btn-primary">Groups</button>
    <button type="button" class="show-users show-results-btn btn btn-default">Users</button>
</div> -->
<label>Order By</label>
<select class="order-results-selector">
    <option value="username" selected>Username</option>
    <option value="firstlast">First, Last Name</option>
    <option value="lastfirst">Last, First Name</option>
    <option value="email">Email</option>
</select>
<ul id="share-box-users" class="form-control"></ul>
<label>Set permissions with the drop-down menu on each user, or click the x to cancel sharing.</label>
<ul id="share-box-share" class="form-control"></ul>
