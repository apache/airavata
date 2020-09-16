import datetime
import unittest

from airavata_django_portal_sdk.util import convert_iso8601_to_datetime


class UtilTestCase(unittest.TestCase):

    def test_convert_iso8601_to_datetime(self):
        now = datetime.datetime.now()
        self.assertEqual(
            convert_iso8601_to_datetime(
                now.isoformat() + "Z"), now)
