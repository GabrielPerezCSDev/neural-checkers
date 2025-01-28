
#!/bin/bash

# Debug: Print working directory
echo "Current directory: $(pwd)"

# Load environment variables
if [ -f .env ]; then
    echo "Found .env file"
    source .env
else
    echo "Error: .env file not found"
    echo "Please create a .env file with required variables"
    exit 1
fi

# Verify required variables are set
REQUIRED_VARS=("VM_USER" "VM_IP" "VM_PATH" "SSH_KEY")
for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        echo "Error: $var is not set in .env"
        exit 1
    fi
done

# Set connection string
CONNECTION="${VM_USER}@${VM_IP}"

# Colors for output
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# Function to push code to VM
push_code() {
    echo -e "${GREEN}Creating remote directory structure...${NC}"
    ssh -i "${SSH_KEY}" "${CONNECTION}" "mkdir -p ${VM_PATH}"

    echo -e "${GREEN}Pushing code to VM...${NC}"
    scp -i "${SSH_KEY}" \
        -r \
        ./src \
        ./build.sh \
        ./.env \
        ./keystore.jks \
        "${CONNECTION}:${VM_PATH}/"
        
    # Add dos2unix conversion after copying
    echo -e "${GREEN}Converting line endings...${NC}"
    ssh -i "${SSH_KEY}" "${CONNECTION}" "cd ${VM_PATH} && dos2unix build.sh"
}

# Function to build code on VM
build_code() {
    echo -e "${GREEN}Building code on VM...${NC}"
    ssh -i "${SSH_KEY}" "${CONNECTION}" "cd ${VM_PATH} && chmod +x build.sh && ./build.sh"
}

# Function to run server on VM
run_backend() {
    echo -e "${GREEN}Running backend on VM...${NC}"
    echo -e "${GREEN}Press Ctrl+C to stop the server${NC}"
    ssh -t -i "${SSH_KEY}" "${CONNECTION}" "cd ${VM_PATH} && java -cp bin main.Main"
}

# Main script logic
case "$1" in
    "push")
        push_code
        ;;
    "build")
        build_code
        ;;
    "run")
        run_backend
        ;;
    "all")
        push_code && build_code && run_backend
        ;;
    *)
        echo "Usage: $0 [push|build|run|all]"
        exit 1
        ;;
esac

exit