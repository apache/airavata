import uuid

from django.db import models


class EmailVerification(models.Model):
    username = models.CharField(max_length=64)
    verification_code = models.CharField(
        max_length=36, unique=True, default=uuid.uuid4)
    created_date = models.DateTimeField(auto_now_add=True)
    verified = models.BooleanField(default=False)
