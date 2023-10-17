declare global {
  interface Filter {
    label: React.ReactNode;
    name: string;
    type: 'char' | 'char-exact' | 'select' | 'checkbox' | 'date' | 'text';
    options?: { label: React.ReactNode; value: string }[];
  }
}

export {};
