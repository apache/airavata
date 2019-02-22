@if($form)
<label for="entity-share">Sharing Settings</label><br />
<button type="button" class="btn btn-default" name="entity-share" id="entity-share">Share With Other Users</button><br />
@else
<h3>Sharing Details</h3>
@endif
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
<div id="shared-users-updated-message"></div>
<div id="shared-users"></div>
@if($form)
<input id="share-settings" name="share-settings" type="hidden" value="{}" />
@endif
