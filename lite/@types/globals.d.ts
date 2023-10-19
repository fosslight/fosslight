declare global {
  interface Filter {
    label: React.ReactNode;
    name: string;
    type: 'char' | 'char-exact' | 'select' | 'checkbox' | 'date' | 'number' | 'text';
    options?: { label: React.ReactNode; value: string }[];
  }
}

export {};
