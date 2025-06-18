
# Register your models here.

from django.contrib import admin

from .models import ApplicationTemplate, ApplicationTemplateContextProcessor


class ApplicationTemplateContextProcessorInline(admin.StackedInline):
    model = ApplicationTemplateContextProcessor
    extra = 1


class ApplicationTemplateAdmin(admin.ModelAdmin):
    fields = ['application_module_id', 'template_path']
    list_display = ['application_module_id', 'template_path', 'updated_by', 'updated']
    inlines = [ApplicationTemplateContextProcessorInline]

    def save_model(self, request, obj, form, change):
        obj.updated_by = request.user
        if not obj.pk:
            obj.created_by = request.user
        return super().save_model(request, obj, form, change)


admin.site.register(ApplicationTemplate, ApplicationTemplateAdmin)
