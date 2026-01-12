import re
import time
from playwright.sync_api import sync_playwright, expect

def run():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        base_url = "http://localhost:8080"

        # 1. Signup
        print(f"Navigating to {base_url}/signup...")
        try:
            page.goto(f"{base_url}/signup", timeout=60000)
        except Exception as e:
            print(f"Failed to load page: {e}")
            return

        username = f"user_{int(time.time())}"
        password = "password123"

        print(f"Registering user: {username}")
        page.fill("input[name='username']", username)
        page.fill("input[name='password']", password)
        page.click("button[type='submit']")

        # 2. Login
        print("Logging in...")
        page.wait_for_url(f"{base_url}/login")
        page.fill("input[name='username']", username)
        page.fill("input[name='password']", password)
        page.click("button[type='submit']")
        page.wait_for_url(f"{base_url}/")
        print("Login successful.")

        # 3. Create Root Document
        print("Creating Root Document...")
        page.click(".sidebar-header button[data-bs-target='#newDocModal']")
        expect(page.locator("#newDocModal")).to_be_visible()
        page.fill("#newDocModal input[name='title']", "Root Doc")
        page.click("#newDocModal button:has-text('Create')")

        page.wait_for_url(re.compile(r"/docs/\d+/edit"))
        print("Root Doc created (in Edit Mode).")

        # 4. Edit Document (Root)
        print("Editing content...")
        page.wait_for_selector(".CodeMirror")
        page.click(".CodeMirror")
        page.keyboard.type("# Hello World\nThis is the root document.")
        page.click("button:has-text('Save')")

        page.wait_for_url(re.compile(r"/docs/\d+$"))
        print("Verifying content...")
        page.wait_for_selector("#viewer")
        viewer_text = page.inner_text("#viewer")
        assert "Hello World" in viewer_text
        print("Content verification passed.")

        # 5. Create Sub-Page (Hierarchy)
        print("Creating Sub-Page...")
        root_doc_url = page.url

        sidebar_item = page.locator("div.group-item", has=page.locator("text='Root Doc'"))
        add_sub_btn = sidebar_item.locator("button.add-sub-btn")
        add_sub_btn.click()

        expect(page.locator("#newDocModal")).to_be_visible()
        parent_id_val = page.input_value("#parentIdInput")
        assert parent_id_val != "", "Parent ID should be set"
        print(f"Parent ID: {parent_id_val}")

        page.fill("#newDocModal input[name='title']", "Child Doc")
        page.click("#newDocModal button:has-text('Create')")

        print("Waiting for navigation to new doc...")
        page.wait_for_url(lambda url: url != root_doc_url and "/docs/" in url and "/edit" in url, timeout=10000)
        new_url = page.url
        print(f"New Doc URL: {new_url}")

        input_val = page.input_value("input[name='title']")
        assert input_val == "Child Doc"
        print("Child Doc title verified in Edit Mode.")

        print("Saving Child Doc...")
        page.click("button:has-text('Save')")
        page.wait_for_url(re.compile(r"/docs/\d+$"))

        expect(page.locator("h2")).to_have_text("Child Doc")
        print("Child Doc created and saved.")

        # 6. Share Functionality
        print("Testing Share functionality...")
        page.click("button:has-text('Share')")
        print("Enabling share (will trigger reload)...")
        with page.expect_navigation():
             page.check("#shareToggle")

        print("Page reloaded. Re-opening share popup...")
        page.click("button:has-text('Share')")

        page.wait_for_selector("#shareUrl", state="visible")
        share_url_path = page.input_value("#shareUrl")
        full_share_url = base_url + share_url_path
        print(f"Share URL: {full_share_url}")

        # 7. Verify Public Access
        print("Verifying public access...")
        context2 = browser.new_context()
        page2 = context2.new_page()
        page2.goto(full_share_url)

        # Shared page uses h1 class="doc-title" not h2
        print("Waiting for h1.doc-title...")
        page2.wait_for_selector("h1.doc-title")
        public_title = page2.inner_text("h1.doc-title")
        assert "Child Doc" in public_title
        print("Public access verified.")

        print("=== ALL VERIFICATION STEPS PASSED ===")
        browser.close()

if __name__ == "__main__":
    run()
