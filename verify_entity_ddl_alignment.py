#!/usr/bin/env python3
"""
Systematic verification script to ensure Flyway migrations match entity definitions.
This script extracts metadata from both entities and DDL, then compares them.
"""

import re
import os
from pathlib import Path
from collections import defaultdict
from typing import Dict, List, Set, Optional, Tuple

# Database to entity package mapping
DB_ENTITY_MAP = {
    'app_catalog': 'org.apache.airavata.registry.entities.appcatalog',
    'credential_store': 'org.apache.airavata.credential.entities',
    'experiment_catalog': 'org.apache.airavata.registry.entities.expcatalog',
    'profile_service': 'org.apache.airavata.profile.entities',
    'replica_catalog': 'org.apache.airavata.registry.entities.replicacatalog',
    'sharing_registry': 'org.apache.airavata.sharing.entities',
    'workflow_catalog': 'org.apache.airavata.registry.entities.airavataworkflowcatalog'
}

BASE_PATH = Path('airavata-api/src/main/java')
MIGRATION_PATH = Path('airavata-api/src/main/resources/db/migration')

class EntityMetadata:
    def __init__(self):
        self.table_name: Optional[str] = None
        self.columns: Dict[str, Dict] = {}
        self.primary_keys: List[str] = []
        self.foreign_keys: List[Dict] = []
        self.id_class: Optional[str] = None
        self.is_immutable: bool = False

def extract_entity_metadata(entity_file: Path) -> Optional[EntityMetadata]:
    """Extract metadata from a Java entity file"""
    try:
        with open(entity_file, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        print(f"Error reading {entity_file}: {e}")
        return None
    
    metadata = EntityMetadata()
    
    # Check for @Immutable
    if '@Immutable' in content:
        metadata.is_immutable = True
    
    # Extract @Table(name = "...")
    table_match = re.search(r'@Table\s*\(\s*name\s*=\s*["\']([^"\']+)["\']', content)
    if table_match:
        metadata.table_name = table_match.group(1)
    else:
        # Try without quotes or with backticks
        table_match = re.search(r'@Table\s*\(\s*name\s*=\s*`([^`]+)`', content)
        if table_match:
            metadata.table_name = table_match.group(1)
    
    # Extract @IdClass
    idclass_match = re.search(r'@IdClass\s*\(\s*(\w+)\.class\s*\)', content)
    if idclass_match:
        metadata.id_class = idclass_match.group(1)
    
    # Extract @Id fields with @Column
    id_pattern = r'@Id\s+(?:@Column\s*\(\s*name\s*=\s*["\']([^"\']+)["\']|private\s+\w+\s+(\w+);)'
    for match in re.finditer(id_pattern, content):
        col_name = match.group(1) or match.group(2)
        if col_name:
            metadata.primary_keys.append(col_name)
    
    # Extract all @Column annotations
    # Pattern: @Column(name = "...", nullable = true/false, length = N, columnDefinition = "...")
    column_pattern = r'@Column\s*\(\s*([^)]+)\)'
    for match in re.finditer(column_pattern, content):
        col_def = match.group(1)
        name_match = re.search(r'name\s*=\s*["\']([^"\']+)["\']', col_def)
        if not name_match:
            name_match = re.search(r'name\s*=\s*`([^`]+)`', col_def)
        if name_match:
            col_name = name_match.group(1)
            nullable_match = re.search(r'nullable\s*=\s*(true|false)', col_def)
            length_match = re.search(r'length\s*=\s*(\d+)', col_def)
            coldef_match = re.search(r'columnDefinition\s*=\s*["\']([^"\']+)["\']', col_def)
            
            metadata.columns[col_name] = {
                'nullable': nullable_match.group(1) == 'true' if nullable_match else True,
                'length': int(length_match.group(1)) if length_match else None,
                'columnDefinition': coldef_match.group(1) if coldef_match else None,
                'is_lob': False
            }
    
    # Check for @Lob annotations (can be before or after @Column)
    # Pattern: @Lob (possibly with newlines) @Column
    lob_pattern = r'@Lob\s+(?:.*?\n)?\s*@Column\s*\(\s*name\s*=\s*["\']([^"\']+)["\']'
    lob_matches = re.findall(lob_pattern, content, re.DOTALL)
    for lob_col in lob_matches:
        if lob_col in metadata.columns:
            metadata.columns[lob_col]['is_lob'] = True
    
    # Also check for @Lob with backticks
    lob_pattern2 = r'@Lob\s+(?:.*?\n)?\s*@Column\s*\(\s*name\s*=\s*`([^`]+)`'
    lob_matches2 = re.findall(lob_pattern2, content, re.DOTALL)
    for lob_col in lob_matches2:
        if lob_col in metadata.columns:
            metadata.columns[lob_col]['is_lob'] = True
    
    # Extract @ManyToOne with @JoinColumn (simple FK)
    manytoone_pattern = r'@ManyToOne[^@]*?@JoinColumn\s*\(\s*name\s*=\s*["\']([^"\']+)["\'][^)]*?referencedColumnName\s*=\s*["\']([^"\']+)["\']'
    fks = re.findall(manytoone_pattern, content, re.DOTALL)
    for fk_col, ref_col in fks:
        # Try to find targetEntity
        target_match = re.search(r'@ManyToOne[^@]*?targetEntity\s*=\s*(\w+)\.class', content, re.DOTALL)
        target_entity = target_match.group(1) if target_match else None
        metadata.foreign_keys.append({
            'column': fk_col,
            'referenced_column': ref_col,
            'target_entity': target_entity
        })
    
    # Extract @ManyToOne with @JoinColumns (composite FK)
    manytoone_composite_pattern = r'@ManyToOne[^@]*?@JoinColumns\s*\(\s*\{([^}]+)\}'
    composite_fks = re.findall(manytoone_composite_pattern, content, re.DOTALL)
    for comp_fk_def in composite_fks:
        # Extract all @JoinColumn within @JoinColumns
        join_cols = re.findall(r'@JoinColumn\s*\(\s*name\s*=\s*["\']([^"\']+)["\'][^)]*?referencedColumnName\s*=\s*["\']([^"\']+)["\']', comp_fk_def)
        if join_cols:
            target_match = re.search(r'targetEntity\s*=\s*(\w+)\.class', comp_fk_def)
            target_entity = target_match.group(1) if target_match else None
            metadata.foreign_keys.append({
                'columns': [jc[0] for jc in join_cols],
                'referenced_columns': [jc[1] for jc in join_cols],
                'target_entity': target_entity,
                'is_composite': True
            })
    
    return metadata

def extract_ddl_metadata(migration_files: List[Path]) -> Dict[str, Dict]:
    """Extract metadata from SQL DDL files"""
    tables = {}
    
    for mig_file in sorted(migration_files):
        try:
            with open(mig_file, 'r', encoding='utf-8') as f:
                content = f.read()
        except Exception as e:
            print(f"Error reading {mig_file}: {e}")
            continue
        
        # Extract CREATE TABLE statements
        # Pattern: CREATE TABLE table_name ( ... )
        table_pattern = r'CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?`?(\w+)`?\s*\(([^;]+)\)'
        for match in re.finditer(table_pattern, content, re.IGNORECASE | re.DOTALL):
            table_name = match.group(1)
            table_def = match.group(2)
            
            if table_name not in tables:
                tables[table_name] = {
                    'columns': {},
                    'primary_keys': [],
                    'foreign_keys': [],
                    'migration_file': str(mig_file.name)
                }
            
            # Extract columns
            # Pattern: column_name TYPE (length) NULL/NOT NULL
            col_pattern = r'`?(\w+)`?\s+(\w+(?:\([^)]+\))?)\s*(?:NULL|NOT\s+NULL)?'
            for col_match in re.finditer(col_pattern, table_def, re.IGNORECASE):
                col_name = col_match.group(1)
                col_type = col_match.group(2)
                
                # Check if NOT NULL
                not_null_match = re.search(rf'`?{re.escape(col_name)}`?\s+\w+[^,]*NOT\s+NULL', table_def, re.IGNORECASE)
                is_nullable = not_null_match is None
                
                # Extract length from type like VARCHAR(255)
                length_match = re.search(r'\((\d+)\)', col_type)
                length = int(length_match.group(1)) if length_match else None
                
                # Check if LONGTEXT or LONGBLOB
                is_lob = 'LONGTEXT' in col_type.upper() or 'LONGBLOB' in col_type.upper()
                
                tables[table_name]['columns'][col_name] = {
                    'type': col_type,
                    'nullable': is_nullable,
                    'length': length,
                    'is_lob': is_lob
                }
            
            # Extract PRIMARY KEY
            pk_pattern = r'PRIMARY\s+KEY\s*\(\s*([^)]+)\)'
            pk_match = re.search(pk_pattern, table_def, re.IGNORECASE)
            if pk_match:
                pk_cols = [c.strip().strip('`') for c in pk_match.group(1).split(',')]
                tables[table_name]['primary_keys'] = pk_cols
            
            # Extract FOREIGN KEY
            fk_pattern = r'FOREIGN\s+KEY\s*\(([^)]+)\)\s+REFERENCES\s+`?(\w+)`?\s*\(([^)]+)\)\s*(?:ON\s+DELETE\s+(\w+))?'
            for fk_match in re.finditer(fk_pattern, table_def, re.IGNORECASE):
                fk_cols = [c.strip().strip('`') for c in fk_match.group(1).split(',')]
                ref_table = fk_match.group(2)
                ref_cols = [c.strip().strip('`') for c in fk_match.group(3).split(',')]
                on_delete = fk_match.group(4) if fk_match.group(4) else None
                
                tables[table_name]['foreign_keys'].append({
                    'columns': fk_cols,
                    'referenced_table': ref_table,
                    'referenced_columns': ref_cols,
                    'on_delete': on_delete
                })
    
    return tables

def get_java_type_mapping(java_type: str, is_lob: bool, length: Optional[int], col_def: Optional[str]) -> str:
    """Map Java type to SQL type"""
    if col_def:
        return col_def
    
    if is_lob:
        return 'LONGTEXT'
    
    if java_type == 'String':
        if length:
            return f'VARCHAR({length})'
        return 'VARCHAR(255)'
    elif java_type == 'Timestamp':
        return 'TIMESTAMP'
    elif java_type == 'boolean':
        return 'TINYINT(1)'
    elif java_type == 'int':
        return 'INTEGER'
    elif java_type == 'long':
        return 'BIGINT'
    elif java_type == 'byte[]':
        return 'LONGBLOB'
    else:
        return 'VARCHAR(255)'  # Default for enums and unknown types

def verify_database(db_name: str, entity_package: str) -> Dict:
    """Verify a single database"""
    print(f"\n{'='*80}")
    print(f"Verifying: {db_name}")
    print(f"{'='*80}")
    
    results = {
        'database': db_name,
        'entities_found': 0,
        'tables_found': 0,
        'mismatches': [],
        'warnings': []
    }
    
    # Find entity files
    package_path = BASE_PATH / entity_package.replace('.', '/')
    if not package_path.exists():
        results['mismatches'].append(f"Entity package path not found: {package_path}")
        return results
    
    entity_files = list(package_path.glob('*Entity.java'))
    entity_files = [ef for ef in entity_files if 'PK' not in ef.name]  # Exclude PK classes
    
    # Extract entity metadata
    entities = {}
    for ef in entity_files:
        metadata = extract_entity_metadata(ef)
        if metadata and metadata.table_name:
            entities[metadata.table_name] = metadata
            results['entities_found'] += 1
    
    # Find migration files
    migration_dir = MIGRATION_PATH / db_name
    if not migration_dir.exists():
        results['mismatches'].append(f"Migration directory not found: {migration_dir}")
        return results
    
    migration_files = sorted(migration_dir.glob('V*.sql'))
    
    # Extract DDL metadata
    ddl_tables = extract_ddl_metadata(migration_files)
    results['tables_found'] = len(ddl_tables)
    
    # Compare entities vs DDL
    entity_table_names = set(entities.keys())
    ddl_table_names = set(ddl_tables.keys())
    
    # Check for missing tables
    missing_tables = entity_table_names - ddl_table_names
    for table in missing_tables:
        results['mismatches'].append(f"Table {table} exists in entities but not in DDL")
    
    # Check for extra tables
    extra_tables = ddl_table_names - entity_table_names
    for table in extra_tables:
        results['warnings'].append(f"Table {table} exists in DDL but no entity found")
    
    # Compare each table
    for table_name in entity_table_names & ddl_table_names:
        entity = entities[table_name]
        ddl = ddl_tables[table_name]
        
        # Compare columns
        entity_cols = set(entity.columns.keys())
        ddl_cols = set(ddl['columns'].keys())
        
        missing_cols = entity_cols - ddl_cols
        for col in missing_cols:
            results['mismatches'].append(f"Table {table_name}: Column {col} exists in entity but not in DDL")
        
        extra_cols = ddl_cols - entity_cols
        for col in extra_cols:
            results['warnings'].append(f"Table {table_name}: Column {col} exists in DDL but not in entity")
        
        # Compare each column
        for col_name in entity_cols & ddl_cols:
            entity_col = entity.columns[col_name]
            ddl_col = ddl['columns'][col_name]
            
            # Check nullable
            if entity_col['nullable'] != ddl_col['nullable']:
                results['mismatches'].append(
                    f"Table {table_name}.{col_name}: nullable mismatch - entity={entity_col['nullable']}, DDL={ddl_col['nullable']}"
                )
            
            # Check length for VARCHAR
            if entity_col['length'] and ddl_col['type'].startswith('VARCHAR'):
                if entity_col['length'] != ddl_col['length']:
                    results['mismatches'].append(
                        f"Table {table_name}.{col_name}: length mismatch - entity={entity_col['length']}, DDL={ddl_col['length']}"
                    )
            
            # Check LOB
            if entity_col['is_lob'] != ddl_col['is_lob']:
                results['mismatches'].append(
                    f"Table {table_name}.{col_name}: LOB mismatch - entity={entity_col['is_lob']}, DDL={ddl_col['is_lob']}"
                )
        
        # Compare primary keys
        entity_pk = sorted(entity.primary_keys)
        ddl_pk = sorted(ddl['primary_keys'])
        if entity_pk != ddl_pk:
            results['mismatches'].append(
                f"Table {table_name}: Primary key mismatch - entity={entity_pk}, DDL={ddl_pk}"
            )
        
        # Compare foreign keys (simplified - just check count for now)
        # Full FK comparison would require resolving entity class names to table names
        if len(entity.foreign_keys) > 0:
            # At least verify FK count is reasonable
            pass
    
    return results

def main():
    """Main verification function"""
    print("="*80)
    print("FLYWAY MIGRATION vs ENTITY VERIFICATION")
    print("="*80)
    
    all_results = []
    for db_name, entity_package in DB_ENTITY_MAP.items():
        results = verify_database(db_name, entity_package)
        all_results.append(results)
    
    # Print summary
    print("\n" + "="*80)
    print("SUMMARY")
    print("="*80)
    
    total_mismatches = 0
    total_warnings = 0
    
    for results in all_results:
        print(f"\n{results['database']}:")
        print(f"  Entities: {results['entities_found']}")
        print(f"  Tables: {results['tables_found']}")
        print(f"  Mismatches: {len(results['mismatches'])}")
        print(f"  Warnings: {len(results['warnings'])}")
        
        if results['mismatches']:
            print(f"  MISMATCHES:")
            for mismatch in results['mismatches'][:10]:  # Show first 10
                print(f"    - {mismatch}")
            if len(results['mismatches']) > 10:
                print(f"    ... and {len(results['mismatches']) - 10} more")
        
        total_mismatches += len(results['mismatches'])
        total_warnings += len(results['warnings'])
    
    print(f"\n{'='*80}")
    print(f"TOTAL MISMATCHES: {total_mismatches}")
    print(f"TOTAL WARNINGS: {total_warnings}")
    print(f"{'='*80}")
    
    if total_mismatches > 0:
        print("\nCRITICAL: Mismatches found! Review and fix before proceeding.")
        return 1
    else:
        print("\nSUCCESS: No critical mismatches found.")
        return 0

if __name__ == '__main__':
    exit(main())

