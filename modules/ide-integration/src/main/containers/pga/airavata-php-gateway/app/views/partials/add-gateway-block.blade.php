<div class="add-tenant col-md-6">
    <div class="form-group required">
        <label class="control-label">Enter Domain Name</label>
        <input type="text" name="domain" class="form-control" required="required"/>
    </div>
    <div class="form-group required">
        <label class="control-label">Enter Desired Gateway Name</label>
        <input type="text" name="gatewayName" class="form-control gatewayName" required="required"/>
    </div>
    <div class="form-group required">
        <label class="control-label">Enter Admin Email Address</label>
        <input type="text" name="admin-email" class="form-control" required="required" @if( isset ( $userInfo["email"])) readonly="true" value="{{ $userInfo['email'] }}" @endif/>
    </div>
    <div class="form-group required">
        <label class="control-label">Enter Admin First Name</label>
        <input type="text" name="admin-firstname" class="form-control" required="required"/>
    </div>
    <div class="form-group required">
        <label class="control-label">Enter Admin Last Name</label>
        <input type="text" name="admin-lastname" class="form-control" required="required"/>
    </div>
    <div class="form-group required">
        <label class="control-label">Enter Admin Username</label>
        <input type="text" name="admin-username" class="form-control" required="required" @if( isset ( $userInfo["username"])) readonly="true" value="{{ $userInfo['username'] }}" @endif/>
    </div>
    <div class="form-group required">
        <label class="control-label">Enter Admin Password</label>
        <input type="password" name="admin-password" class="form-control" required="required"/>
    </div>
    <div class="form-group required">
        <label class="control-label">Re-enter Admin Password</label>
        <input type="password" name="admin-password-confirm" class="form-control" required="required"/>
    </div>
    <div class="form-group required">
        <input type="submit" class="col-md-2 form-control btn btn-primary" value="Register"/>
    </div>
</div>
<div class="col-md-6 loading-gif hide"><img  src='{{URL::to('/')}}/assets/ajax-loader.gif'/></div>
<div class="col-md-6 alert alert-danger gateway-error hide"></div>
<div class="col-md-6 alert alert-success gateway-success hide"></div>