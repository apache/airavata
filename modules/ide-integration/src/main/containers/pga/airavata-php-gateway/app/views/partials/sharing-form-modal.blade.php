<!-- Reusable modal dialog for sharing UI -->
<div id="share-box" class="modal-fade" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" id="share-box-x" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h3 class="modal-title">Share this {{ $entityName }}</h3>
            </div>
            <div class="modal-body">
                @include('partials/sharing-form-body')
            </div>
            <div class="modal-footer">
                <div id="share-box-error-message">
                </div>
                <button type="button" id="share-box-button" class="btn btn-primary">Update</button>
                <button type="button" id="share-box-close" class="btn btn-default" data-dismiss="modal">Cancel</button>
            </div>
        </div>
    </div>
</div>
