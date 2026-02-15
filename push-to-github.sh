#!/bin/bash

# AO3 Reader - GitHub Push Script
# This script helps you push the project to GitHub

echo "🚀 AO3 Reader - GitHub Push Script"
echo "=================================="
echo ""

# Check if git remote exists
if git remote | grep -q "origin"; then
    echo "✅ Git remote 'origin' already configured"
    git remote -v
else
    echo "📝 Please enter your GitHub username:"
    read -p "Username: " GITHUB_USERNAME

    echo ""
    echo "📝 Please enter your repository name (default: ao3-reader-android):"
    read -p "Repository name: " REPO_NAME
    REPO_NAME=${REPO_NAME:-ao3-reader-android}

    echo ""
    echo "Adding remote: https://github.com/$GITHUB_USERNAME/$REPO_NAME.git"
    git remote add origin "https://github.com/$GITHUB_USERNAME/$REPO_NAME.git"
fi

echo ""
echo "📤 Pushing to GitHub..."
git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Successfully pushed to GitHub!"
    echo ""
    echo "🌐 View your repository at:"
    REMOTE_URL=$(git remote get-url origin)
    echo "$REMOTE_URL" | sed 's/\.git$//'
    echo ""
    echo "📋 Next steps:"
    echo "  1. Visit your repository on GitHub"
    echo "  2. Add topics: android, kotlin, jetpack-compose, fanfiction, ao3"
    echo "  3. Add description: 'Native Android fanfiction reader for AO3'"
    echo "  4. Build the APK (see BUILD_INSTRUCTIONS.md)"
else
    echo ""
    echo "❌ Push failed. Common issues:"
    echo "  1. Repository doesn't exist on GitHub - create it first at https://github.com/new"
    echo "  2. Authentication failed - you may need to set up a Personal Access Token"
    echo "  3. Network issues - check your internet connection"
    echo ""
    echo "For authentication, see: https://docs.github.com/en/authentication"
fi
