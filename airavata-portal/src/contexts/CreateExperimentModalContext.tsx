"use client";

import { createContext, useContext, useState, ReactNode } from "react";
import type { Application } from "@/types";
import { CreateExperimentModal } from "@/components/experiment/CreateExperimentModal";

interface CreateExperimentModalContextType {
  openModal: (options?: { application?: Application; projectId?: string }) => void;
  closeModal: () => void;
}

const CreateExperimentModalContext = createContext<CreateExperimentModalContextType | undefined>(undefined);

export function CreateExperimentModalProvider({ children }: { children: ReactNode }) {
  const [isOpen, setIsOpen] = useState(false);
  const [initialApplication, setInitialApplication] = useState<Application | undefined>();
  const [initialProjectId, setInitialProjectId] = useState<string | undefined>();

  const openModal = (options?: { application?: Application; projectId?: string }) => {
    setInitialApplication(options?.application);
    setInitialProjectId(options?.projectId);
    setIsOpen(true);
  };

  const closeModal = () => {
    setIsOpen(false);
    setTimeout(() => {
      setInitialApplication(undefined);
      setInitialProjectId(undefined);
    }, 200);
  };

  return (
    <CreateExperimentModalContext.Provider value={{ openModal, closeModal }}>
      {children}
      <CreateExperimentModal
        open={isOpen}
        onOpenChange={setIsOpen}
        initialApplication={initialApplication}
        initialProjectId={initialProjectId}
      />
    </CreateExperimentModalContext.Provider>
  );
}

export function useCreateExperimentModal() {
  const context = useContext(CreateExperimentModalContext);
  if (context === undefined) {
    throw new Error("useCreateExperimentModal must be used within a CreateExperimentModalProvider");
  }
  return context;
}
