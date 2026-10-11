import os
import shutil
import subprocess
import zipfile

ITERATIONS_DIR = "./iteracje"

def run_git(args):
    """Wykonuje polecenie git i zwraca wynik."""
    result = subprocess.run(["git"] + args, text=True, capture_output=True)
    if result.returncode != 0:
        print(f"[BŁĄD GIT]: {result.stderr.strip()}")
    return result.stdout.strip()

def run_git_commit(title, body):
    """Tworzy commit w Git z tytułem oraz wieloliniowym opisem."""
    args = ["git", "commit", "-m", title]
    if body:
        args.extend(["-m", body])
    result = subprocess.run(args, text=True, capture_output=True)
    if result.returncode != 0:
        print(f"[BŁĄD COMMIT]: {result.stderr.strip()}")

def main():
    # 1. Inicjalizacja lokalnego repozytorium Git (jeśli nie istnieje)
    if not os.path.exists(".git"):
        run_git(["init"])
        run_git(["branch", "-M", "main"])
        print("[INFO]: Zainicjowano nowe repozytorium Git na gałęzi 'main'.")

    # 2. Wykrycie i posortowanie plików ZIP z iteracjami
    if not os.path.exists(ITERATIONS_DIR):
        print(f"[BŁĄD]: Katalog '{ITERATIONS_DIR}' nie istnieje.")
        return

    zip_files = sorted([f for f in os.listdir(ITERATIONS_DIR) if f.lower().endswith(".zip")])

    if not zip_files:
        print("[BŁĄD]: Nie znaleziono plików .zip w katalogu iteracji.")
        return

    print(f"[INFO]: Znaleziono {len(zip_files)} kroków do przetworzenia.\n")

    # 3. Pętla przetwarzająca kolejne iteracje
    for idx, zip_name in enumerate(zip_files, start=1):
        zip_path = os.path.join(ITERATIONS_DIR, zip_name)
        base_name = os.path.splitext(zip_name)[0]
        txt_path = os.path.join(ITERATIONS_DIR, f"{base_name}.txt")

        print(f"[{idx}/{len(zip_files)}] Przetwarzanie: {zip_name}...")

        # Czyszczenie katalogu roboczego (z wyłączeniem plików systemowych/skryptu)
        for item in os.listdir("."):
            if item in [".git", "iteracje", "commit_builder.py", ".gitignore"]:
                continue
            path = os.path.join(".", item)
            if os.path.isfile(path):
                os.remove(path)
            elif os.path.isdir(path):
                shutil.rmtree(path)

        # Rozpakowanie archiwum ZIP aktualnej iteracji
        with zipfile.ZipFile(zip_path, 'r') as zip_ref:
            zip_ref.extractall(".")

        # Przygotowanie nagłówka i treści commita z pliku .txt
        commit_title = f"Krok {base_name.replace('krok-', '')}: {base_name}"
        commit_body = ""

        if os.path.exists(txt_path):
            with open(txt_path, "r", encoding="utf-8") as f:
                lines = [line.strip() for line in f.readlines() if line.strip()]
                if lines:
                    commit_title = lines[0]
                    commit_body = "\n".join(lines[1:])

        # Rejestracja zmian i wykonanie commita w Git
        run_git(["add", "."])
        run_git_commit(commit_title, commit_body)

    print("\n[SUKCES]: Automatyczna budowa historii commitów została zakończona.")

if __name__ == "__main__":
    main()