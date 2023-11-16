# 🎵 mp3analyzer

Welcome to **mp3analyzer** – your go-to tool for delving into the world of MP3 audio files! Uncover the secrets hidden in the frame headers of your favorite tunes. 🚀

## What is mp3analyzer? 🤔

mp3analyzer is a sleek ZIO HTTP server designed to analyze MP3 file headers and extract key audio details such as MPEG version, layer, sampling rate, and bitrate. Whether you're an audiophile, a developer, or just curious, mp3analyzer provides you with the insights you need in a simple and efficient manner. 🎧

## Features 🌟

- **MPEG Version Discovery:** Find out the MPEG version of your MP3 files.
- **Layer Identification:** Uncover which layer your audio file is using.
- **Sampling Rate Analysis:** Determine the sampling rate to understand the quality.
- **Bitrate Extraction:** Know the bitrate for insights into file size and quality.

## How to Use mp3analyzer 🛠️

1. **Clone the Repository:**

    ```bash
    git clone https://github.com/your-username/mp3analyzer.git
    ```

2. **Run the Server:**

    Navigate to the project directory and run:

    ```bash
    
    sbt run
    ```

3. **Analyze Your MP3:**

    Send a request to the server with your MP3 file. Use your preferred tool to make a request to `localhost:8000/analyze`.

## Endpoint 📍

`POST localhost:8000/analyze`

Send your MP3 file in the request, and the mp3analyzer server will return the analysis in a clear, concise format.

## Contributions 🤝

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are greatly appreciated.

    Fork the Project
    Create your Feature Branch (git checkout -b feature/AmazingFeature)
    Commit your Changes (git commit -m 'Add some AmazingFeature')
    Push to the Branch (git push origin feature/AmazingFeature)
    Open a Pull Request
    

