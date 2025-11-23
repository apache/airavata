package output

import (
	"encoding/csv"
	"encoding/json"
	"fmt"
	"io"
	"reflect"
	"strings"

	"github.com/olekukonko/tablewriter"
)

// Format represents the output format
type Format string

const (
	FormatTable Format = "table"
	FormatJSON  Format = "json"
	FormatCSV   Format = "csv"
)

// Formatter handles output formatting
type Formatter struct {
	format Format
	writer io.Writer
}

// NewFormatter creates a new formatter
func NewFormatter(format Format, writer io.Writer) *Formatter {
	return &Formatter{
		format: format,
		writer: writer,
	}
}

// WriteTable writes data as a table
func (f *Formatter) WriteTable(data interface{}, headers []string) error {
	table := tablewriter.NewWriter(f.writer)
	table.SetHeader(headers)
	table.SetBorder(true)
	table.SetCenterSeparator("|")
	table.SetColumnSeparator("|")
	table.SetRowSeparator("-")
	table.SetHeaderLine(true)
	table.SetTablePadding(" ")
	table.SetNoWhiteSpace(true)
	table.SetAutoWrapText(false)
	table.SetReflowDuringAutoWrap(false)

	// Convert data to rows
	rows := f.convertToRows(data)
	for _, row := range rows {
		table.Append(row)
	}

	table.Render()
	return nil
}

// WriteJSON writes data as JSON
func (f *Formatter) WriteJSON(data interface{}) error {
	encoder := json.NewEncoder(f.writer)
	encoder.SetIndent("", "  ")
	return encoder.Encode(data)
}

// WriteCSV writes data as CSV
func (f *Formatter) WriteCSV(data interface{}, headers []string) error {
	writer := csv.NewWriter(f.writer)
	defer writer.Flush()

	// Write headers
	if err := writer.Write(headers); err != nil {
		return fmt.Errorf("failed to write CSV headers: %w", err)
	}

	// Convert data to rows
	rows := f.convertToRows(data)
	for _, row := range rows {
		if err := writer.Write(row); err != nil {
			return fmt.Errorf("failed to write CSV row: %w", err)
		}
	}

	return nil
}

// Write writes data in the specified format
func (f *Formatter) Write(data interface{}, headers []string) error {
	switch f.format {
	case FormatTable:
		return f.WriteTable(data, headers)
	case FormatJSON:
		return f.WriteJSON(data)
	case FormatCSV:
		return f.WriteCSV(data, headers)
	default:
		return fmt.Errorf("unsupported format: %s", f.format)
	}
}

// convertToRows converts data to string rows for table/CSV output
func (f *Formatter) convertToRows(data interface{}) [][]string {
	var rows [][]string

	// Handle different data types
	switch v := data.(type) {
	case []map[string]interface{}:
		for _, item := range v {
			row := make([]string, 0, len(item))
			for _, value := range item {
				row = append(row, f.formatValue(value))
			}
			rows = append(rows, row)
		}
	case []interface{}:
		for _, item := range v {
			row := f.convertItemToRow(item)
			rows = append(rows, row)
		}
	case map[string]interface{}:
		// Single item
		row := f.convertItemToRow(v)
		rows = append(rows, row)
	default:
		// Try to convert using reflection
		rows = f.convertUsingReflection(data)
	}

	return rows
}

// convertItemToRow converts a single item to a string row
func (f *Formatter) convertItemToRow(item interface{}) []string {
	var row []string

	switch v := item.(type) {
	case map[string]interface{}:
		for _, value := range v {
			row = append(row, f.formatValue(value))
		}
	case []interface{}:
		for _, value := range v {
			row = append(row, f.formatValue(value))
		}
	default:
		row = append(row, f.formatValue(v))
	}

	return row
}

// convertUsingReflection converts data using reflection
func (f *Formatter) convertUsingReflection(data interface{}) [][]string {
	var rows [][]string

	val := reflect.ValueOf(data)
	if val.Kind() == reflect.Ptr {
		val = val.Elem()
	}

	switch val.Kind() {
	case reflect.Slice, reflect.Array:
		for i := 0; i < val.Len(); i++ {
			item := val.Index(i)
			row := f.convertStructToRow(item)
			rows = append(rows, row)
		}
	case reflect.Struct:
		row := f.convertStructToRow(val)
		rows = append(rows, row)
	}

	return rows
}

// convertStructToRow converts a struct to a string row
func (f *Formatter) convertStructToRow(val reflect.Value) []string {
	var row []string

	if val.Kind() == reflect.Ptr {
		val = val.Elem()
	}

	if val.Kind() != reflect.Struct {
		return []string{f.formatValue(val.Interface())}
	}

	typ := val.Type()
	for i := 0; i < val.NumField(); i++ {
		field := val.Field(i)
		fieldType := typ.Field(i)

		// Skip unexported fields
		if !field.CanInterface() {
			continue
		}

		// Use field name or json tag
		_ = fieldType.Name
		if jsonTag := fieldType.Tag.Get("json"); jsonTag != "" {
			parts := strings.Split(jsonTag, ",")
			if parts[0] != "" {
				_ = parts[0]
			}
		}

		value := f.formatValue(field.Interface())
		row = append(row, value)
	}

	return row
}

// formatValue formats a value for display
func (f *Formatter) formatValue(value interface{}) string {
	if value == nil {
		return ""
	}

	switch v := value.(type) {
	case string:
		return v
	case int, int8, int16, int32, int64:
		return fmt.Sprintf("%d", v)
	case uint, uint8, uint16, uint32, uint64:
		return fmt.Sprintf("%d", v)
	case float32, float64:
		return fmt.Sprintf("%.2f", v)
	case bool:
		return fmt.Sprintf("%t", v)
	case []interface{}:
		var parts []string
		for _, item := range v {
			parts = append(parts, f.formatValue(item))
		}
		return strings.Join(parts, ", ")
	case map[string]interface{}:
		// Convert map to key=value pairs
		var parts []string
		for key, val := range v {
			parts = append(parts, fmt.Sprintf("%s=%s", key, f.formatValue(val)))
		}
		return strings.Join(parts, ", ")
	default:
		return fmt.Sprintf("%v", v)
	}
}

// WriteError writes an error message
func (f *Formatter) WriteError(err error) error {
	errorData := map[string]string{
		"error": err.Error(),
	}
	return f.WriteJSON(errorData)
}

// WriteSuccess writes a success message
func (f *Formatter) WriteSuccess(message string) error {
	successData := map[string]string{
		"message": message,
		"status":  "success",
	}
	return f.WriteJSON(successData)
}
