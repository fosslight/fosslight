FROM node:18

# Copy Source Codes
COPY . /data/fosslight
WORKDIR /data/fosslight

# Install Node.js Packages
RUN npm install

# Set Environment Variable for Build
ARG PRODUCT_MODE
ENV NEXT_PUBLIC_PRODUCT_MODE=$PRODUCT_MODE

# Bundle Source Codes
RUN npm run build

# Run Front-end Server
CMD ["npm", "run", "start"]
