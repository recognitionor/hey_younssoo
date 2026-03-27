import os
import shutil

src_dir = '/Users/jhlee/workspace/beumProject'
dest_dir = '/Users/jhlee/workspace/youngsso'

def ignore_patterns(dir, contents):
    return [c for c in contents if c in ['.git', '.idea', 'build', '.gradle', '.fleet'] or (dir.endswith('composeApp') and c == 'build') or (dir.endswith('iosApp') and c == 'build')]

# Copy everything from beumProject to youngsso
for item in os.listdir(src_dir):
    src_item = os.path.join(src_dir, item)
    dest_item = os.path.join(dest_dir, item)
    if ignore_patterns(src_dir, [item]):
        continue
    if os.path.isdir(src_item):
        shutil.copytree(src_item, dest_item, dirs_exist_ok=True, ignore=ignore_patterns)
    else:
        shutil.copy2(src_item, dest_item)

# Replace com.kal.bium with com.bium.youngssoo
for root, dirs, files in os.walk(dest_dir):
    for filename in files:
        if filename.endswith(('.kt', '.xml', '.gradle', '.kts', '.json', '.properties', '.pbxproj', '.swift', '.storyboard', '.plist', '.pro')):
            filepath = os.path.join(root, filename)
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                new_content = content.replace('com.kal.bium', 'com.bium.youngssoo')
                new_content = new_content.replace('beumProject', 'youngsso')
                
                if new_content != content:
                    with open(filepath, 'w', encoding='utf-8') as f:
                        f.write(new_content)
            except Exception as e:
                pass

# Refactor package directories: com/kal/bium -> com/bium/youngssoo
for root, dirs, files in os.walk(dest_dir, topdown=False):
    for d in list(dirs):
        if d == 'bium' and os.path.basename(root) == 'kal':
            com_dir = os.path.dirname(root) # .../com
            kal_dir = root # .../com/kal
            bium_dir = os.path.join(kal_dir, d) # .../com/kal/bium
            
            new_bium_dir = os.path.join(com_dir, 'bium')
            new_youngssoo_dir = os.path.join(new_bium_dir, 'youngssoo')
            
            os.makedirs(new_bium_dir, exist_ok=True)
            # rename bium -> youngssoo
            os.rename(bium_dir, new_youngssoo_dir)
            try:
                os.rmdir(kal_dir)
            except:
                pass
            
            # Since we modify the directory structure bottom-up, we don't need to patch `dirs` 
            # as it's topdown=False, meaning we visit children before parents.
            
# Clean up target's specific features
common_main_youngssoo = os.path.join(dest_dir, 'composeApp/src/commonMain/kotlin/com/bium/youngssoo')
if os.path.exists(common_main_youngssoo):
    features_to_remove = ['community', 'content', 'home', 'level', 'login', 'notice', 'myinfo', 'main', 'splash']
    for feature in features_to_remove:
        path = os.path.join(common_main_youngssoo, feature)
        if os.path.exists(path):
            shutil.rmtree(path)
            
print("Setup script finished.")
