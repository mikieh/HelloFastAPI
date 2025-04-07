# HelloFastAPI

A simple FastAPI application.

## Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd HelloFastAPI
   ```

2. Create a virtual environment and activate it:
   ```bash
   python -m venv venv
   # On Windows
   .\venv\Scripts\activate
   # On macOS/Linux
   source venv/bin/activate
   ```

3. Install the dependencies:
   ```bash
   pip install -r requirements.txt
   ```

## Usage

Run the FastAPI application using Uvicorn:

```bash
uvicorn main:app --reload
```

Open your browser and navigate to `http://127.0.0.1:8000`.

You can also access the automatic interactive API documentation at `http://127.0.0.1:8000/docs`. 