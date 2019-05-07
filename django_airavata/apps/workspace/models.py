
# Create your models here.

from django.db import models


class User_Files(models.Model):
    file_name = models.CharField(max_length=50)
    file_dpu = models.CharField(max_length=500)
    # username = models.CharField(primary_key=True,max_length=20) not required
