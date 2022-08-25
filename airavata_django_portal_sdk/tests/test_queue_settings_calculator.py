import unittest

from airavata.model.experiment.ttypes import ExperimentModel

from airavata_django_portal_sdk import queue_settings_calculators
from airavata_django_portal_sdk.decorators import queue_settings_calculator


class QueueSettingsCalculatorDecoratorTestCase(unittest.TestCase):

    def tearDown(self) -> None:
        queue_settings_calculators.reset_registry()

    def test_registration_no_arguments(self):

        @queue_settings_calculator
        def foo(request, experiment_model):
            return {}

        self.assertEqual(len(queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS), 1)
        self.assertEqual(queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS[0].name, 'foo')
        self.assertEqual(queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS[0].id, 'airavata_django_portal_sdk.tests.test_queue_settings_calculator:foo')

    def test_registration_id_only(self):
        @queue_settings_calculator(id="my-foo")
        def foo(request, experiment_model):
            return {}

        self.assertEqual(len(queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS), 1)
        self.assertEqual(queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS[0].name, 'foo')
        self.assertEqual(queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS[0].id, 'my-foo')

    def test_registration_name_only(self):

        @queue_settings_calculator(name="Genome-based calculator")
        def foo(request, experiment_model):
            return {}

        self.assertEqual(len(queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS), 1)
        self.assertEqual(queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS[0].name, 'Genome-based calculator')
        self.assertEqual(queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS[0].id, 'airavata_django_portal_sdk.tests.test_queue_settings_calculator:foo')

    def test_registration_id_and_name(self):

        @queue_settings_calculator(id="genome-based", name="Genome-based calculator")
        def foo(request, experiment_model):
            return {}

        self.assertEqual(len(queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS), 1)
        self.assertEqual(queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS[0].name, 'Genome-based calculator')
        self.assertEqual(queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS[0].id, 'genome-based')


class QueueSettingsTestCase(unittest.TestCase):

    def tearDown(self) -> None:
        queue_settings_calculators.reset_registry()

    def test_invocation(self):

        @queue_settings_calculator(id="my-calc")
        def foo(request, experiment_model):
            return {'queueName': 'shared'}

        request = None
        experiment_model = ExperimentModel()
        result = queue_settings_calculators.calculate_queue_settings("my-calc", request, experiment_model)
        self.assertIn('queueName', result)
        self.assertEqual(result['queueName'], 'shared')

    def test_invocation_missing_calculator(self):

        @queue_settings_calculator(id="my-calc")
        def foo(request, experiment_model):
            return {'queueName': 'shared'}

        request = None
        experiment_model = ExperimentModel()
        with self.assertRaises(LookupError):
            queue_settings_calculators.calculate_queue_settings("my-calc-missing", request, experiment_model)

    def test_get_all(self):

        @queue_settings_calculator(id="my-calc")
        def foo(request, experiment_model):
            return {'queueName': 'shared'}

        all_calculators = queue_settings_calculators.get_all()
        self.assertIsNot(all_calculators, queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS, "verify list is a copy")
        self.assertListEqual(all_calculators, queue_settings_calculators.QUEUE_SETTINGS_CALCULATORS)
        self.assertEqual(all_calculators[0].id, 'my-calc')
