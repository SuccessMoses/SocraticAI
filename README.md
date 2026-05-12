# SocraticAI: The Local-First Agentic Tutor

**SocraticAI** is a state-of-the-art agentic framework for Android, designed to transform student learning through the Socratic method. Rather than providing direct answers, SocraticAI guides students to discover solutions themselves—truly "teaching them to fish."

Leveraging the native multimodality of **Gemma 4** and high-performance on-device inference via **Google AI Edge's LiteRT**, SocraticAI provides a private, intelligent, and responsive learning environment.

---

## 🎓 The Philosophy: "Teach to Fish"
Standard AI chatbots often hinder learning by providing immediate answers. SocraticAI uses a fine-tuned Socratic model and a multi-step agentic loop to:
1.  **Analyze Knowledge Gaps**: Identify what the student doesn't yet understand from their notes.
2.  **Scaffold Learning**: Break complex problems into manageable sub-concepts.
3.  **Guide via Inquiry**: Ask insightful, grounded questions that lead the student to the next logical step.

## 🏗️ Architecture: Grounded in Open-Notebook
The framework's "Agentic DNA" is directly migrated from the [lfnovo/open-notebook](https://github.com/lfnovo/open-notebook) project, adapted for the constraints and opportunities of mobile hardware.

*   **Socratic State Machine**: A Kotlin-native orchestrator that manages the tutoring loop (`INIT` -> `CONTEXT_CHECK` -> `GENERATE_GUIDE` -> `EVALUATE`), mirroring the LangGraph workflows of proven agentic systems.
*   **Optimistic Map-Reduce**: A sophisticated context management algorithm that allows the model to "read" large textbooks or handwritten notebooks by chunking and synthesizing data locally.
*   **Agentic RAG & Artifacts**: Every AI-generated question is an "Artifact" linked via a graph relationship back to its original source in the student's study materials, ensuring high-integrity grounding and provenance.

## 🚀 Core Capabilities

### 1. Multimodal On-Device Intelligence
*   **Vision-Ready Reasoning**: Uses Gemma 4 to directly "see" and reason about handwritten notes and diagrams in student notebooks.
*   **Hardware-Accelerated Inference**: Optimized for mobile NPUs and GPUs using LiteRT-LM for near-instant, offline responses.

### 2. Intelligent Agentic Routing
*   **Router-Worker Architecture**: Intelligently routes tasks between **Gemma 270M** (for instant intent detection and UI feedback) and **Gemma 4B** (for deep Socratic reasoning).
*   **Dynamic Resource Optimization**: Minimizes battery drain and thermal impact by activating the high-parameter reasoning model only when complex cognition is required.

### 3. Local-First Sovereignty & Privacy
*   **100% Offline Execution**: All text extraction, vector embeddings, and LLM inferences happen entirely on-device, ensuring zero dependency on cloud APIs.
*   **Secure Local Memory**: Uses **ObjectBox 4.0** for local vector storage, guaranteeing that sensitive student data never leaves the device.

---

## 🛠️ Technology Stack
*   **Language**: Kotlin (Jetpack Compose)
*   **AI Engine**: LiteRT & LiteRT-LM (Gemma 4 / Gemma 270M)
*   **Vector Database**: ObjectBox 4.0
*   **Orchestration**: Custom Agentic Graph (Grounded in Open-Notebook)

---

## 🛡️ Rules of Engagement
The agent's behavior is strictly governed by `AGENTS.md`, which serves as the "Socratic Constitution" for the framework, defining the boundaries and personas used during tutoring sessions.
