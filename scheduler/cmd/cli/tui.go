package main

import (
	"fmt"
	"strings"
	"time"

	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
)

// Task represents a task in the experiment
type Task struct {
	ID              string        `json:"id"`
	Name            string        `json:"name"`
	Status          string        `json:"status"`
	Progress        float64       `json:"progress"`
	WorkerID        string        `json:"workerId"`
	ComputeResource string        `json:"computeResource"`
	StartTime       time.Time     `json:"startTime"`
	EndTime         time.Time     `json:"endTime"`
	Duration        time.Duration `json:"duration"`
	Message         string        `json:"message"`
}

// Experiment represents an experiment being monitored
type Experiment struct {
	ID             string    `json:"id"`
	Name           string    `json:"name"`
	Status         string    `json:"status"`
	TotalTasks     int       `json:"totalTasks"`
	CompletedTasks int       `json:"completedTasks"`
	FailedTasks    int       `json:"failedTasks"`
	RunningTasks   int       `json:"runningTasks"`
	PendingTasks   int       `json:"pendingTasks"`
	Progress       float64   `json:"progress"`
	CreatedAt      time.Time `json:"createdAt"`
	UpdatedAt      time.Time `json:"updatedAt"`
	Tasks          []Task    `json:"tasks"`
}

// TUIState represents the state of the TUI
type TUIState struct {
	experiment   *Experiment
	selectedTask int
	scrollOffset int
	connected    bool
	lastUpdate   time.Time
	error        string
	showHelp     bool
	width        int
	height       int
}

// Styles for the TUI
var (
	titleStyle = lipgloss.NewStyle().
			Bold(true).
			Foreground(lipgloss.Color("#FAFAFA")).
			Background(lipgloss.Color("#7D56F4")).
			Padding(0, 1)

	headerStyle = lipgloss.NewStyle().
			Bold(true).
			Foreground(lipgloss.Color("#FAFAFA")).
			Background(lipgloss.Color("#626262")).
			Padding(0, 1)

	statusStyle = lipgloss.NewStyle().
			Bold(true).
			Padding(0, 1)

	completedStyle = statusStyle.Copy().
			Foreground(lipgloss.Color("#FAFAFA")).
			Background(lipgloss.Color("#04B575"))

	failedStyle = statusStyle.Copy().
			Foreground(lipgloss.Color("#FAFAFA")).
			Background(lipgloss.Color("#FF5F87"))

	runningStyle = statusStyle.Copy().
			Foreground(lipgloss.Color("#FAFAFA")).
			Background(lipgloss.Color("#3C91E6"))

	pendingStyle = statusStyle.Copy().
			Foreground(lipgloss.Color("#FAFAFA")).
			Background(lipgloss.Color("#F2CC8F"))

	selectedStyle = lipgloss.NewStyle().
			Bold(true).
			Foreground(lipgloss.Color("#7D56F4")).
			Background(lipgloss.Color("#F5F5F5"))

	helpStyle = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#626262")).
			Italic(true)

	errorStyle = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#FF5F87")).
			Bold(true)

	progressBarStyle = lipgloss.NewStyle().
				Foreground(lipgloss.Color("#04B575"))
)

// NewTUIState creates a new TUI state
func NewTUIState(experiment *Experiment) *TUIState {
	return &TUIState{
		experiment:   experiment,
		selectedTask: 0,
		scrollOffset: 0,
		connected:    true,
		lastUpdate:   time.Now(),
		showHelp:     false,
		width:        80,
		height:       24,
	}
}

// Init initializes the TUI
func (s *TUIState) Init() tea.Cmd {
	return nil
}

// Update handles TUI updates
func (s *TUIState) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	switch msg := msg.(type) {
	case tea.WindowSizeMsg:
		s.width = msg.Width
		s.height = msg.Height
		return s, nil

	case tea.KeyMsg:
		switch msg.String() {
		case "q", "ctrl+c":
			return s, tea.Quit

		case "h", "?":
			s.showHelp = !s.showHelp
			return s, nil

		case "j", "down":
			if s.selectedTask < len(s.experiment.Tasks)-1 {
				s.selectedTask++
				s.adjustScroll()
			}
			return s, nil

		case "k", "up":
			if s.selectedTask > 0 {
				s.selectedTask--
				s.adjustScroll()
			}
			return s, nil

		case "g":
			s.selectedTask = 0
			s.scrollOffset = 0
			return s, nil

		case "G":
			s.selectedTask = len(s.experiment.Tasks) - 1
			s.adjustScroll()
			return s, nil

		case "r":
			// Refresh - this would trigger a refresh command
			return s, nil
		}

	case WebSocketMessage:
		s.handleWebSocketMessage(msg)
		return s, nil

	case error:
		s.error = msg.Error()
		return s, nil
	}

	return s, nil
}

// View renders the TUI
func (s *TUIState) View() string {
	if s.showHelp {
		return s.renderHelp()
	}

	var content strings.Builder

	// Title
	content.WriteString(titleStyle.Render("Airavata Experiment Monitor"))
	content.WriteString("\n\n")

	// Experiment header
	content.WriteString(s.renderExperimentHeader())
	content.WriteString("\n")

	// Progress summary
	content.WriteString(s.renderProgressSummary())
	content.WriteString("\n")

	// Tasks table
	content.WriteString(s.renderTasksTable())
	content.WriteString("\n")

	// Status bar
	content.WriteString(s.renderStatusBar())

	return content.String()
}

// handleWebSocketMessage processes WebSocket messages
func (s *TUIState) handleWebSocketMessage(msg WebSocketMessage) {
	s.lastUpdate = time.Now()

	switch msg.Type {
	case WebSocketMessageTypeExperimentProgress:
		if progress, err := ParseExperimentProgress(msg); err == nil {
			s.updateExperimentProgress(progress)
		}

	case WebSocketMessageTypeTaskProgress:
		if progress, err := ParseTaskProgress(msg); err == nil {
			s.updateTaskProgress(progress)
		}

	case WebSocketMessageTypeTaskUpdated:
		s.updateTaskFromMessage(msg)

	case WebSocketMessageTypeExperimentUpdated:
		s.updateExperimentFromMessage(msg)

	case WebSocketMessageTypeError:
		s.error = fmt.Sprintf("WebSocket error: %v", msg.Error)
	}
}

// updateExperimentProgress updates experiment progress
func (s *TUIState) updateExperimentProgress(progress *ExperimentProgress) {
	if s.experiment.ID == progress.ExperimentID {
		s.experiment.TotalTasks = progress.TotalTasks
		s.experiment.CompletedTasks = progress.CompletedTasks
		s.experiment.FailedTasks = progress.FailedTasks
		s.experiment.RunningTasks = progress.RunningTasks
		s.experiment.PendingTasks = progress.PendingTasks
		s.experiment.Progress = progress.Progress
		s.experiment.Status = progress.Status
		s.experiment.UpdatedAt = time.Now()
	}
}

// updateTaskProgress updates task progress
func (s *TUIState) updateTaskProgress(progress *TaskProgress) {
	for i, task := range s.experiment.Tasks {
		if task.ID == progress.TaskID {
			s.experiment.Tasks[i].Progress = progress.Progress
			s.experiment.Tasks[i].Status = progress.Status
			s.experiment.Tasks[i].Message = progress.Message
			if progress.WorkerID != "" {
				s.experiment.Tasks[i].WorkerID = progress.WorkerID
			}
			if progress.ComputeResource != "" {
				s.experiment.Tasks[i].ComputeResource = progress.ComputeResource
			}
			break
		}
	}
}

// updateTaskFromMessage updates a task from WebSocket message
func (s *TUIState) updateTaskFromMessage(msg WebSocketMessage) {
	// Implementation would parse task data from message
	// and update the corresponding task in the experiment
}

// updateExperimentFromMessage updates experiment from WebSocket message
func (s *TUIState) updateExperimentFromMessage(msg WebSocketMessage) {
	// Implementation would parse experiment data from message
	// and update the experiment state
}

// adjustScroll adjusts the scroll offset based on selected task
func (s *TUIState) adjustScroll() {
	visibleTasks := s.height - 15 // Account for headers and status bar
	if visibleTasks < 1 {
		visibleTasks = 1
	}

	if s.selectedTask < s.scrollOffset {
		s.scrollOffset = s.selectedTask
	} else if s.selectedTask >= s.scrollOffset+visibleTasks {
		s.scrollOffset = s.selectedTask - visibleTasks + 1
	}
}

// renderExperimentHeader renders the experiment header
func (s *TUIState) renderExperimentHeader() string {
	var content strings.Builder

	content.WriteString(headerStyle.Render("Experiment Details"))
	content.WriteString("\n")
	content.WriteString(fmt.Sprintf("ID:     %s\n", s.experiment.ID))
	content.WriteString(fmt.Sprintf("Name:   %s\n", s.experiment.Name))
	content.WriteString(fmt.Sprintf("Status: %s\n", s.getStatusStyle(s.experiment.Status).Render(s.experiment.Status)))
	content.WriteString(fmt.Sprintf("Created: %s\n", s.experiment.CreatedAt.Format("2006-01-02 15:04:05")))

	return content.String()
}

// renderProgressSummary renders the progress summary
func (s *TUIState) renderProgressSummary() string {
	var content strings.Builder

	content.WriteString(headerStyle.Render("Progress Summary"))
	content.WriteString("\n")

	// Overall progress bar
	progressBar := FormatProgressBar(s.experiment.Progress, 40)
	content.WriteString(fmt.Sprintf("Overall: %s\n", progressBar))

	// Task counts
	content.WriteString(fmt.Sprintf("Total: %d | ", s.experiment.TotalTasks))
	content.WriteString(completedStyle.Render(fmt.Sprintf("Completed: %d", s.experiment.CompletedTasks)))
	content.WriteString(" | ")
	content.WriteString(runningStyle.Render(fmt.Sprintf("Running: %d", s.experiment.RunningTasks)))
	content.WriteString(" | ")
	content.WriteString(pendingStyle.Render(fmt.Sprintf("Pending: %d", s.experiment.PendingTasks)))
	content.WriteString(" | ")
	content.WriteString(failedStyle.Render(fmt.Sprintf("Failed: %d", s.experiment.FailedTasks)))
	content.WriteString("\n")

	return content.String()
}

// renderTasksTable renders the tasks table
func (s *TUIState) renderTasksTable() string {
	var content strings.Builder

	content.WriteString(headerStyle.Render("Tasks"))
	content.WriteString("\n")

	if len(s.experiment.Tasks) == 0 {
		content.WriteString("No tasks available\n")
		return content.String()
	}

	// Table header
	header := fmt.Sprintf("%-4s %-20s %-12s %-15s %-10s %-8s",
		"#", "Name", "Status", "Worker", "Progress", "Duration")
	content.WriteString(header)
	content.WriteString("\n")
	content.WriteString(strings.Repeat("-", len(header)))
	content.WriteString("\n")

	// Calculate visible range
	visibleTasks := s.height - 15
	if visibleTasks < 1 {
		visibleTasks = 1
	}

	start := s.scrollOffset
	end := start + visibleTasks
	if end > len(s.experiment.Tasks) {
		end = len(s.experiment.Tasks)
	}

	// Render visible tasks
	for i := start; i < end; i++ {
		task := s.experiment.Tasks[i]
		line := s.renderTaskRow(i, task)
		content.WriteString(line)
		content.WriteString("\n")
	}

	return content.String()
}

// renderTaskRow renders a single task row
func (s *TUIState) renderTaskRow(index int, task Task) string {
	// Truncate long names
	name := task.Name
	if len(name) > 20 {
		name = name[:17] + "..."
	}

	// Format status
	status := s.getStatusStyle(task.Status).Render(task.Status)

	// Format worker ID
	worker := task.WorkerID
	if len(worker) > 15 {
		worker = worker[:12] + "..."
	}

	// Format progress
	progress := FormatProgressBar(task.Progress, 8)

	// Format duration
	duration := task.Duration.String()
	if len(duration) > 8 {
		duration = duration[:5] + "..."
	}

	line := fmt.Sprintf("%-4d %-20s %-12s %-15s %-10s %-8s",
		index+1, name, status, worker, progress, duration)

	// Highlight selected row
	if index == s.selectedTask {
		line = selectedStyle.Render(line)
	}

	return line
}

// renderStatusBar renders the status bar
func (s *TUIState) renderStatusBar() string {
	var content strings.Builder

	// Connection status
	connStatus := "🔴 Disconnected"
	if s.connected {
		connStatus = "🟢 Connected"
	}

	// Last update time
	lastUpdate := s.lastUpdate.Format("15:04:05")

	// Help text
	helpText := "Press 'h' for help, 'q' to quit"

	content.WriteString(strings.Repeat("-", s.width))
	content.WriteString("\n")
	content.WriteString(fmt.Sprintf("%s | Last update: %s | %s", connStatus, lastUpdate, helpText))

	// Error message
	if s.error != "" {
		content.WriteString("\n")
		content.WriteString(errorStyle.Render("Error: " + s.error))
	}

	return content.String()
}

// renderHelp renders the help screen
func (s *TUIState) renderHelp() string {
	var content strings.Builder

	content.WriteString(titleStyle.Render("Airavata CLI Help"))
	content.WriteString("\n\n")

	content.WriteString(headerStyle.Render("Navigation"))
	content.WriteString("\n")
	content.WriteString("j, ↓    Move down\n")
	content.WriteString("k, ↑    Move up\n")
	content.WriteString("g       Go to first task\n")
	content.WriteString("G       Go to last task\n")
	content.WriteString("\n")

	content.WriteString(headerStyle.Render("Actions"))
	content.WriteString("\n")
	content.WriteString("r       Refresh experiment status\n")
	content.WriteString("h, ?    Toggle this help screen\n")
	content.WriteString("q, Ctrl+C  Quit\n")
	content.WriteString("\n")

	content.WriteString(headerStyle.Render("Status Colors"))
	content.WriteString("\n")
	content.WriteString(completedStyle.Render("Completed") + " - Task finished successfully\n")
	content.WriteString(failedStyle.Render("Failed") + " - Task failed with error\n")
	content.WriteString(runningStyle.Render("Running") + " - Task currently executing\n")
	content.WriteString(pendingStyle.Render("Pending") + " - Task waiting to start\n")
	content.WriteString("\n")

	content.WriteString(helpStyle.Render("Press 'h' again to return to the main view"))

	return content.String()
}

// getStatusStyle returns the appropriate style for a status
func (s *TUIState) getStatusStyle(status string) lipgloss.Style {
	switch strings.ToLower(status) {
	case "completed", "success":
		return completedStyle
	case "failed", "error":
		return failedStyle
	case "running", "executing":
		return runningStyle
	case "pending", "queued":
		return pendingStyle
	default:
		return statusStyle
	}
}

// RunTUI runs the TUI for experiment monitoring
func RunTUI(experiment *Experiment, wsClient *WebSocketClient) error {
	state := NewTUIState(experiment)

	// Subscribe to experiment updates
	if err := wsClient.Subscribe("experiment", experiment.ID); err != nil {
		return fmt.Errorf("failed to subscribe to experiment updates: %w", err)
	}

	// Create the program
	program := tea.NewProgram(state, tea.WithAltScreen())

	// Handle WebSocket messages
	go func() {
		for {
			select {
			case msg := <-wsClient.GetMessageChan():
				program.Send(msg)
			case err := <-wsClient.GetErrorChan():
				program.Send(err)
			}
		}
	}()

	// Run the program
	_, err := program.Run()
	return err
}
