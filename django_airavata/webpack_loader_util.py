"""Utilities for working with webpack_loader."""
import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def create_webpack_loader_config(static_root=None):
    return {
        'COMMON': {
            'BUNDLE_DIR_NAME': 'common/dist/',
            'STATS_FILE': os.path.join(
                static_root if static_root else
                os.path.join(
                    BASE_DIR,
                    'django_airavata',
                    'static',
                ),
                'common',
                'dist',
                'webpack-stats.json'),
        },
        'ADMIN': {
            'BUNDLE_DIR_NAME': 'django_airavata_admin/dist/',
            'STATS_FILE': os.path.join(
                static_root if static_root else
                os.path.join(
                    BASE_DIR,
                    'django_airavata',
                    'apps',
                    'admin',
                    'static',
                ),
                'django_airavata_admin',
                'dist',
                'webpack-stats.json'),
        },
        'DATAPARSERS': {
            'BUNDLE_DIR_NAME': 'django_airavata_dataparsers/dist/',
            'STATS_FILE': os.path.join(
                static_root if static_root else
                os.path.join(
                    BASE_DIR,
                    'django_airavata',
                    'apps',
                    'dataparsers',
                    'static',
                ),
                'django_airavata_dataparsers',
                'dist',
                'webpack-stats.json'),
        },
        'GROUPS': {
            'BUNDLE_DIR_NAME': 'django_airavata_groups/dist/',
            'STATS_FILE': os.path.join(
                static_root if static_root else
                os.path.join(
                    BASE_DIR,
                    'django_airavata',
                    'apps',
                    'groups',
                    'static',
                ),
                'django_airavata_groups',
                'dist',
                'webpack-stats.json'),
        },
        'WORKSPACE': {
            'BUNDLE_DIR_NAME': 'django_airavata_workspace/dist/',
            'STATS_FILE': os.path.join(
                static_root if static_root else
                os.path.join(
                    BASE_DIR,
                    'django_airavata',
                    'apps',
                    'workspace',
                    'static',
                ),
                'django_airavata_workspace',
                'dist',
                'webpack-stats.json'),
        },
    }
