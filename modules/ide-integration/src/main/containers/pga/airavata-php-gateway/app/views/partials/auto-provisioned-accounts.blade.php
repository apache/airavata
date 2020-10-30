
<div class="row">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Automatically Configured Accounts</h3>
        </div>
        <div class="panel-body">
            <p>
                The following displays the results from attempting to
                automatically configure your account on the following
                cluster(s). All successfully configured accounts can be used
                from this gateway.
            </p>
            <table class="table">
                <thead>
                    <tr>
                        <th>Status</th>
                        <th>Hostname</th>
                        <th>Additional Info</th>
                        <th>Error Message</th>
                    </tr>
                </thead>
                <tbody>
                    @foreach ($auto_provisioned_accounts as $auto_provisioned_account)
                        <tr>
                            <td>
                                @if ($auto_provisioned_account["errorMessage"] != null)
                                    <span class="glyphicon glyphicon-warning-sign" style="color: red;"></span> FAILED
                                    <p>
                                        <small>See <strong>Error Message</strong> for more information and contact Gateway Admin for help.</small>
                                    </p>
                                @elseif ($auto_provisioned_account["accountIsMissing"] === true)
                                    <span class="glyphicon glyphicon-user" style="color: red;"></span> ACCOUNT MISSING
                                    <p>
                                        <small>See <strong>Additional Info</strong> for more information on how to create your account on {{{ $auto_provisioned_account["hostname"]}}}.</small>
                                    </p>
                                @elseif ($auto_provisioned_account["userComputeResourcePreference"] != null && $auto_provisioned_account["userComputeResourcePreference"]->validated)
                                    <span class="glyphicon glyphicon-ok" style="color: green;"></span> OK
                                @else
                                    <span class="glyphicon glyphicon-question-sign" style="color: grey;"></span> UNKNOWN
                                @endif
                            </td>
                            <td>{{{ $auto_provisioned_account["hostname"] }}}</td>
                            <td>
                            {{-- Only display additional info if account isn't completely setup --}}
                            @if ($auto_provisioned_account["userComputeResourcePreference"] == null || !$auto_provisioned_account["userComputeResourcePreference"]->validated)
                                {{-- Not escaping HTML to allow Gateway Admin to put HTML into additionalInfo field --}}
                                {{ $auto_provisioned_account["additionalInfo"] }}
                            @endif
                            </td>
                            <td>{{{ $auto_provisioned_account["errorMessage"] }}}</td>
                        </tr>
                    @endforeach
                </tbody>
            </table>
        </div>
    </div>
</div>