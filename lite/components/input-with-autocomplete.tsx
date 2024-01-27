import clsx from 'clsx';
import React, { useEffect, useState } from 'react';

export default function InputWithAutocomplete({
  value,
  setValue,
  pickValue,
  onBlur,
  options,
  placeholder
}: {
  value: string;
  setValue: (value: string) => void;
  pickValue?: (value: string) => void;
  onBlur?: () => void;
  options: { value: string; label: string }[];
  placeholder: string;
}) {
  const [activeOptionIdx, setActiveOptionIdx] = useState(0);
  const [showOptions, setShowOptions] = useState(false);
  const [filteredOptions, setFilteredOptions] = useState<{ value: string; label: string }[]>([]);

  useEffect(() => {
    setFilteredOptions(
      value
        ? options.filter((option) => option.value.toLowerCase().indexOf(value.toLowerCase()) > -1)
        : options
    );
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value, options]);

  return (
    <div className="relative w-full">
      <input
        className="w-full px-2 py-1 border border-darkgray outline-none"
        value={value}
        onChange={(e) => {
          const input = e.currentTarget.value;

          setValue(input);
          setActiveOptionIdx(0);
          setShowOptions(true);
        }}
        onKeyDown={(e) => {
          if (e.key === 'Enter') {
            e.preventDefault();

            const selectedOption = filteredOptions[activeOptionIdx];
            let valueToPick = value;

            if (showOptions && filteredOptions && selectedOption) {
              valueToPick = filteredOptions[activeOptionIdx].value;
              setValue(valueToPick);
              setActiveOptionIdx(0);
              setShowOptions(false);
            }

            if (pickValue) pickValue(valueToPick);
          }

          if (
            showOptions &&
            filteredOptions.length > 0 &&
            ['ArrowUp', 'ArrowDown'].includes(e.key)
          ) {
            let newActiveOptionIdx;

            if (e.key === 'ArrowUp') {
              if (activeOptionIdx === 0) {
                return;
              }
              newActiveOptionIdx = activeOptionIdx - 1;
            } else {
              if (activeOptionIdx === filteredOptions.length - 1) {
                return;
              }
              newActiveOptionIdx = activeOptionIdx + 1;
            }

            setActiveOptionIdx(newActiveOptionIdx);

            const optionElement = Array.from(document.querySelectorAll('.autocomplete-option'))[
              newActiveOptionIdx
            ] as HTMLElement;
            const container = optionElement.parentElement as HTMLElement;

            if (
              container.scrollTop + container.offsetHeight <
              optionElement.offsetTop + optionElement.offsetHeight
            ) {
              container.scrollTop =
                optionElement.offsetTop + optionElement.offsetHeight - container.offsetHeight;
            } else if (optionElement.offsetTop < container.scrollTop) {
              container.scrollTop = optionElement.offsetTop;
            }
          }
        }}
        onFocus={() => {
          setActiveOptionIdx(0);
          setShowOptions(true);
        }}
        onBlur={() => {
          setActiveOptionIdx(0);
          setShowOptions(false);
          if (pickValue) pickValue(value);
          if (onBlur) onBlur();
        }}
        placeholder={placeholder}
      />
      {showOptions && filteredOptions.length > 0 && (
        <ul className="absolute top-full left-0 right-0 max-h-[100px] mt-1 bg-white rounded shadow-[0_1px_4px_1px_rgb(34,34,34,0.2)] overflow-y-auto z-10">
          {filteredOptions.map((option, idx) => {
            return (
              <li
                key={option.value}
                className={clsx(
                  'autocomplete-option px-2 py-1 cursor-pointer hover:bg-sky-50',
                  idx === activeOptionIdx && '!bg-sky-100'
                )}
                onMouseDown={() => {
                  setValue(option.value);
                  setActiveOptionIdx(0);
                  setShowOptions(false);
                  if (pickValue) pickValue(option.value);
                }}
              >
                {option.label}
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
